package io.github.reasure3.content.hive

import com.simibubi.create.AllItems
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModBlockEntities
import io.github.reasure3.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import java.util.Optional

@EventBusSubscriber(modid = CreateApiculture.MOD_ID)
object ReinforcedBeehiveConversion {
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onPlacedBeehiveRightClick(event: PlayerInteractEvent.RightClickBlock) {
        val level = event.level
        val pos = event.pos
        val state = level.getBlockState(pos)
        val stack = event.itemStack
        val player = event.entity

        if (!canConvertPlacedBeehive(state, stack)) {
            return
        }

        if (!player.mayBuild()) {
            return
        }

        event.cancellationResult = InteractionResult.sidedSuccess(level.isClientSide)
        event.setCanceled(true)

        if (level !is ServerLevel) {
            return
        }

        playPlacedBeehiveBreakEffects(level, pos, state)
        convertPlacedBeehive(level, pos, state)
        if (!player.isCreative) {
            stack.shrink(1)
        }

        level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1.0f, 1.45f)
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
    }

    @SubscribeEvent
    fun onDeployerRecipeSearch(event: DeployerRecipeSearchEvent) {
        val input = event.inventory.getItem(0)
        val held = event.inventory.getItem(1)
        if (!canDeployBeehiveItem(input, held)) {
            return
        }

        event.addRecipe({ Optional.of(createRuntimeDeployerRecipe(input)) }, DEPLOYER_RECIPE_PRIORITY)
    }

    internal fun createManualApplicationRecipe(): RecipeHolder<ManualApplicationRecipe> =
        RecipeHolder(
            MANUAL_APPLICATION_RECIPE_ID,
            createBeehiveApplicationRecipe(::ManualApplicationRecipe, MANUAL_APPLICATION_RECIPE_ID),
        )

    internal fun createDisplayDeployerRecipe(): RecipeHolder<DeployerApplicationRecipe> =
        RecipeHolder(
            DEPLOYER_RECIPE_ID,
            createBeehiveApplicationRecipe(::DeployerApplicationRecipe, DEPLOYER_RECIPE_ID),
        )

    private fun canConvertPlacedBeehive(state: BlockState, stack: ItemStack): Boolean =
        state.`is`(Blocks.BEEHIVE) && stack.`is`(AllItems.ANDESITE_ALLOY.get())

    private fun canDeployBeehiveItem(input: ItemStack, held: ItemStack): Boolean =
        input.`is`(Blocks.BEEHIVE.asItem()) && held.`is`(AllItems.ANDESITE_ALLOY.get())

    private fun convertPlacedBeehive(level: ServerLevel, pos: BlockPos, state: BlockState) {
        val registryAccess = level.registryAccess()
        val beehiveData = (level.getBlockEntity(pos) as? BeehiveBlockEntity)?.saveCustomOnly(registryAccess)
        val convertedState = createPlacedBeehiveState(state)

        level.setBlock(pos, convertedState, Block.UPDATE_ALL)

        if (beehiveData != null) {
            loadPlacedBeehiveData(level, pos, beehiveData)
        }
    }

    private fun createPlacedBeehiveState(state: BlockState): BlockState =
        ModBlocks.REINFORCED_BEEHIVE.get().defaultBlockState()
            .setValue(BeehiveBlock.FACING, state.getValue(BeehiveBlock.FACING))
            .setValue(BeehiveBlock.HONEY_LEVEL, state.getValue(BeehiveBlock.HONEY_LEVEL))

    private fun createRuntimeDeployerRecipe(input: ItemStack): RecipeHolder<DeployerApplicationRecipe> {
        val recipe = createBeehiveApplicationRecipe(::DeployerApplicationRecipe, DEPLOYER_RECIPE_ID)

        recipe.enforceNextResult { createDeployerResultStack(input) }
        return RecipeHolder(DEPLOYER_RECIPE_ID, recipe)
    }

    private fun <R : ItemApplicationRecipe> createBeehiveApplicationRecipe(
        factory: ItemApplicationRecipe.Factory<R>,
        id: ResourceLocation,
    ): R =
        ItemApplicationRecipe.Builder(factory, id)
            .require(Blocks.BEEHIVE)
            .require(AllItems.ANDESITE_ALLOY.get())
            .output(ModBlocks.REINFORCED_BEEHIVE.get())
            .build()

    private fun createDeployerResultStack(input: ItemStack): ItemStack {
        val output = input.transmuteCopy(ModBlocks.REINFORCED_BEEHIVE.get(), 1)
        val blockEntityData = output.get(DataComponents.BLOCK_ENTITY_DATA) ?: return output

        BlockItem.setBlockEntityData(output, ModBlockEntities.REINFORCED_BEEHIVE.get(), blockEntityData.copyTag())
        return output
    }

    private fun playPlacedBeehiveBreakEffects(level: ServerLevel, pos: BlockPos, state: BlockState) {
        // Broadcasts the vanilla block-break sound and particles to clients without destroying the block.
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state))
    }

    private fun loadPlacedBeehiveData(level: ServerLevel, pos: BlockPos, data: CompoundTag) {
        val blockEntity = level.getBlockEntity(pos) as? ReinforcedBeehiveBlockEntity ?: return

        blockEntity.loadCustomOnly(data, level.registryAccess())
        blockEntity.setChanged()
    }

    private val MANUAL_APPLICATION_RECIPE_ID: ResourceLocation =
        CreateApiculture.id("item_application/reinforced_beehive")
    private val DEPLOYER_RECIPE_ID: ResourceLocation = CreateApiculture.id("deploying/reinforced_beehive")
    private const val DEPLOYER_RECIPE_PRIORITY = 1000
}
