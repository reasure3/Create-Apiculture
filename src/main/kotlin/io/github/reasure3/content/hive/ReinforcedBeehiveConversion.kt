package io.github.reasure3.content.hive

import com.simibubi.create.AllItems
import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
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

    private fun canConvertPlacedBeehive(state: BlockState, stack: ItemStack): Boolean =
        state.`is`(Blocks.BEEHIVE) && stack.`is`(AllItems.ANDESITE_ALLOY.get())

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

    private fun playPlacedBeehiveBreakEffects(level: ServerLevel, pos: BlockPos, state: BlockState) {
        // Broadcasts the vanilla block-break sound and particles to clients without destroying the block.
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state))
    }

    private fun loadPlacedBeehiveData(level: ServerLevel, pos: BlockPos, data: CompoundTag) {
        val blockEntity = level.getBlockEntity(pos) as? ReinforcedBeehiveBlockEntity ?: return

        blockEntity.loadCustomOnly(data, level.registryAccess())
        blockEntity.setChanged()
    }
}
