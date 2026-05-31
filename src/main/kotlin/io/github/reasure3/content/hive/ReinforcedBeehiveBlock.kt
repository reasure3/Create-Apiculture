package io.github.reasure3.content.hive

import com.mojang.serialization.MapCodec
import com.simibubi.create.content.equipment.wrench.IWrenchable
import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModBlockEntities
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.EnchantmentTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Bee
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.item.PrimedTnt
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.WitherSkull
import net.minecraft.world.entity.vehicle.MinecartTNT
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.BlockItemStateProperties
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.neoforge.common.ItemAbilities
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.BlockEvent

/**
 * Ports the vanilla beehive behaviors this block still needs without extending
 * [net.minecraft.world.level.block.BeehiveBlock].
 *
 * Reinforced hives must not expose the vanilla honey-level property: Create's VanillaFluidTargets
 * treats blocks with BlockStateProperties.LEVEL_HONEY as vanilla hives, drains 250mB, and resets
 * that level to 0. Honey storage therefore lives in ReinforcedBeehiveBlockEntity, while this block
 * only exposes a derived display_honey_level for models and redstone.
 *
 * Vanilla honey-drip particles are intentionally omitted to keep large automated apiaries cheap.
 * Fire-nearby emergency release is also intentionally omitted because reinforced hives are fire-safe.
 */
class ReinforcedBeehiveBlock(properties: Properties) : BaseEntityBlock(properties), IWrenchable {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(DISPLAY_HONEY_LEVEL, 0),
        )
    }

    override fun codec(): MapCodec<ReinforcedBeehiveBlock> = CODEC

    override fun getRotatedBlockState(originalState: BlockState, targetedFace: Direction): BlockState =
        if (targetedFace.axis == Direction.Axis.Y) {
            originalState.setValue(FACING, originalState.getValue(FACING).getClockWise(Direction.Axis.Y))
        } else {
            originalState
        }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        ReinforcedBeehiveBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>,
    ): BlockEntityTicker<T>? =
        if (level.isClientSide) {
            null
        } else {
            createTickerHelper(
                blockEntityType,
                ModBlockEntities.REINFORCED_BEEHIVE.get(),
                ReinforcedBeehiveBlockEntity::serverTick,
            )
        }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        val honeyMb = storedHoneyMb(stack)
        val beeCount = stack.getOrDefault(DataComponents.BEES, emptyList()).size
        if (!hasContents(honeyMb, beeCount)) {
            return
        }

        tooltipComponents += createContentTooltip(HONEY_TOOLTIP, honeyMb, ReinforcedBeehiveBlockEntity.HONEY_CAPACITY_MB, "mB")
        tooltipComponents += createContentTooltip(BEES_TOOLTIP, beeCount, BeehiveBlockEntity.MAX_OCCUPANTS)
    }

    override fun onSneakWrenched(state: BlockState, context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos
        val player = context.player ?: return InteractionResult.SUCCESS

        if (level !is ServerLevel) {
            return InteractionResult.SUCCESS
        }

        val event = BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player)
        NeoForge.EVENT_BUS.post(event)
        if (event.isCanceled) {
            return InteractionResult.SUCCESS
        }

        val blockEntity = level.getBlockEntity(pos)
        if (!player.isCreative || shouldGiveCreativeStack(blockEntity)) {
            player.inventory.placeItemBackInInventory(createPreservedStack(level, state, blockEntity))
        }

        state.spawnAfterBreak(level, pos, ItemStack.EMPTY, true)
        level.destroyBlock(pos, false)
        IWrenchable.playRemoveSound(level, pos)
        return InteractionResult.SUCCESS
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)

    override fun hasAnalogOutputSignal(state: BlockState): Boolean =
        true

    override fun getAnalogOutputSignal(blockState: BlockState, level: Level, pos: BlockPos): Int =
        blockState.getValue(DISPLAY_HONEY_LEVEL)

    override fun playerDestroy(
        level: Level,
        player: Player,
        pos: BlockPos,
        state: BlockState,
        blockEntity: BlockEntity?,
        stack: ItemStack,
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, stack)
        if (!level.isClientSide && blockEntity is BeehiveBlockEntity) {
            if (!EnchantmentHelper.hasTag(stack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
                blockEntity.emptyAllLivingFromHive(player, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY)
                level.updateNeighbourForOutputSignal(pos, this)
                angerNearbyBees(level, pos)
            }

            if (player is ServerPlayer) {
                CriteriaTriggers.BEE_NEST_DESTROYED.trigger(player, state, stack, blockEntity.occupantCount)
            }
        }
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        val blockEntity = level.getBlockEntity(pos)
        if (
            !level.isClientSide &&
            player.isCreative &&
            level.gameRules.getBoolean(GameRules.RULE_DOBLOCKDROPS) &&
            shouldGiveCreativeStack(blockEntity)
        ) {
            val stack = createPreservedStack(level as ServerLevel, state, blockEntity)
            val itemEntity = ItemEntity(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
            itemEntity.setDefaultPickUpDelay()
            level.addFreshEntity(itemEntity)
        }

        return super.playerWillDestroy(level, pos, state, player)
    }

    override fun getDrops(state: BlockState, params: LootParams.Builder): MutableList<ItemStack> {
        val explosiveEntity = params.getOptionalParameter(LootContextParams.THIS_ENTITY)
        val blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
        if (isExplosive(explosiveEntity) && blockEntity is BeehiveBlockEntity) {
            blockEntity.emptyAllLivingFromHive(null, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY)
        }

        val tool = params.getOptionalParameter(LootContextParams.TOOL)
        if (
            blockEntity is ReinforcedBeehiveBlockEntity &&
            tool != null &&
            EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)
        ) {
            return mutableListOf(createPreservedStack(params.level, state, blockEntity))
        }

        return super.getDrops(state, params)
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult,
    ): ItemInteractionResult {
        val blockEntity = level.getBlockEntity(pos) as? ReinforcedBeehiveBlockEntity
            ?: return super.useItemOn(stack, state, level, pos, player, hand, hitResult)

        if (blockEntity.storedHoneyMb < ReinforcedBeehiveBlockEntity.MANUAL_HONEY_UNIT_MB) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        }

        val usedItem = stack.item
        val harvested = when {
            stack.canPerformAction(ItemAbilities.SHEARS_HARVEST) -> {
                level.playSound(player, player.x, player.y, player.z, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f)
                if (!level.isClientSide && blockEntity.tryConsumeManualUnit()) {
                    popResource(level, pos, ItemStack(Items.HONEYCOMB, SHEARED_HONEYCOMB_COUNT))
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
                    level.gameEvent(player, GameEvent.SHEAR, pos)
                }
                true
            }
            stack.`is`(Items.GLASS_BOTTLE) -> {
                level.playSound(player, player.x, player.y, player.z, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f)
                if (!level.isClientSide && blockEntity.tryConsumeManualUnit()) {
                    stack.shrink(1)
                    giveHoneyBottle(player, hand, stack)
                    level.gameEvent(player, GameEvent.FLUID_PICKUP, pos)
                }
                true
            }
            else -> false
        }

        if (!harvested) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        }

        if (!level.isClientSide) {
            player.awardStat(Stats.ITEM_USED.get(usedItem))
            if (!CampfireBlock.isSmokeyPos(level, pos)) {
                if (!blockEntity.isEmpty) {
                    angerNearbyBees(level, pos)
                }
                blockEntity.emptyAllLivingFromHive(player, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY)
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.setValue(FACING, mirror.mirror(state.getValue(FACING)))

    override fun getRenderShape(state: BlockState): RenderShape =
        RenderShape.MODEL

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, DISPLAY_HONEY_LEVEL)
    }

    private fun giveHoneyBottle(player: Player, hand: InteractionHand, stack: ItemStack) {
        val honeyBottle = ItemStack(Items.HONEY_BOTTLE)
        if (stack.isEmpty) {
            player.setItemInHand(hand, honeyBottle)
        } else if (!player.inventory.add(honeyBottle)) {
            player.drop(honeyBottle, false)
        }
    }

    private fun createPreservedStack(level: ServerLevel, state: BlockState, blockEntity: BlockEntity?): ItemStack {
        val stack = ItemStack(this)

        blockEntity?.saveToItem(stack, level.registryAccess())
        setDisplayState(stack, state.getValue(DISPLAY_HONEY_LEVEL))
        return stack
    }

    private fun shouldGiveCreativeStack(blockEntity: BlockEntity?): Boolean {
        val beeCount = (blockEntity as? BeehiveBlockEntity)?.occupantCount ?: 0
        val honeyMb = (blockEntity as? ReinforcedBeehiveBlockEntity)?.storedHoneyMb ?: 0
        return hasContents(honeyMb, beeCount)
    }

    private fun storedHoneyMb(stack: ItemStack): Int {
        val blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA)
        if (blockEntityData != null) {
            return readStoredHoneyMb(blockEntityData)
        }

        val displayHoneyLevel =
            stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(DISPLAY_HONEY_LEVEL) ?: 0
        return displayHoneyLevel * ReinforcedBeehiveBlockEntity.MANUAL_HONEY_UNIT_MB
    }

    @Suppress("DEPRECATION")
    private fun readStoredHoneyMb(blockEntityData: CustomData): Int {
        // Tooltip rendering only reads this tag; avoid copying the full block entity data on hover.
        return ReinforcedBeehiveBlockEntity.readStoredHoneyMb(blockEntityData.unsafe)
    }

    private fun hasContents(honeyMb: Int, beeCount: Int): Boolean =
        honeyMb > 0 || beeCount > 0

    private fun createContentTooltip(labelKey: String, amount: Int, max: Int, unit: String? = null): Component {
        val suffix = unit?.let { " $it" }.orEmpty()
        return Component.empty()
            .append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY))
            .append(createContentValueComponent(amount, max, suffix))
    }

    private fun createContentValueComponent(amount: Int, max: Int, suffix: String): Component {
        val color = if (amount >= max) ChatFormatting.GREEN else ChatFormatting.GRAY
        return Component.literal("$amount / $max$suffix").withStyle(color)
    }

    private fun angerNearbyBees(level: Level, pos: BlockPos) {
        val bees = level.getEntitiesOfClass(Bee::class.java, AABB(pos).inflate(8.0, 6.0, 8.0))
        if (bees.isEmpty()) {
            return
        }

        val players = level.getEntitiesOfClass(Player::class.java, AABB(pos).inflate(8.0, 6.0, 8.0))
        if (players.isEmpty()) {
            return
        }

        for (bee in bees) {
            if (bee.target == null) {
                bee.target = Util.getRandom(players, level.random)
            }
        }
    }

    private fun isExplosive(entity: Entity?): Boolean =
        entity is PrimedTnt ||
            entity is Creeper ||
            entity is WitherSkull ||
            entity is WitherBoss ||
            entity is MinecartTNT

    companion object {
        const val MAX_HONEY_LEVELS = 5
        private const val SHEARED_HONEYCOMB_COUNT = 3

        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val DISPLAY_HONEY_LEVEL: IntegerProperty =
            IntegerProperty.create("display_honey_level", 0, MAX_HONEY_LEVELS)
        val CODEC: MapCodec<ReinforcedBeehiveBlock> = simpleCodec(::ReinforcedBeehiveBlock)
        private const val HONEY_TOOLTIP = "tooltip.${CreateApiculture.MOD_ID}.reinforced_beehive.honey"
        private const val BEES_TOOLTIP = "tooltip.${CreateApiculture.MOD_ID}.reinforced_beehive.bees"

        fun displayHoneyLevel(storedHoneyMb: Int): Int =
            (storedHoneyMb / ReinforcedBeehiveBlockEntity.MANUAL_HONEY_UNIT_MB).coerceIn(0, MAX_HONEY_LEVELS)

        fun setDisplayState(stack: ItemStack, displayHoneyLevel: Int) {
            if (displayHoneyLevel <= 0) {
                stack.remove(DataComponents.BLOCK_STATE)
                return
            }

            stack.set(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY.with(DISPLAY_HONEY_LEVEL, displayHoneyLevel.coerceAtMost(MAX_HONEY_LEVELS)),
            )
        }
    }
}
