package io.github.reasure3.content.hive

import com.mojang.serialization.MapCodec
import com.simibubi.create.content.equipment.wrench.IWrenchable
import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModBlockEntities
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.BlockItemStateProperties
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.BlockEvent

class ReinforcedBeehiveBlock(properties: Properties) : BeehiveBlock(properties), IWrenchable {
    override fun codec(): MapCodec<BeehiveBlock> = CODEC

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
                BeehiveBlockEntity::serverTick
            )
        }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        val honeyLevel =
            stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(HONEY_LEVEL) ?: 0
        val beeCount = stack.getOrDefault(DataComponents.BEES, emptyList()).size
        if (!hasContents(honeyLevel, beeCount)) {
            return
        }

        tooltipComponents += createContentTooltip(HONEY_TOOLTIP, honeyLevel, MAX_HONEY_LEVELS)
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
        if (!player.isCreative || shouldGiveCreativeStack(state, blockEntity)) {
            player.inventory.placeItemBackInInventory(createPreservedStack(level, state, blockEntity))
        }

        state.spawnAfterBreak(level, pos, ItemStack.EMPTY, true)
        level.destroyBlock(pos, false)
        IWrenchable.playRemoveSound(level, pos)
        return InteractionResult.SUCCESS
    }

    private fun createPreservedStack(level: ServerLevel, state: BlockState, blockEntity: BlockEntity?): ItemStack {
        val stack = ItemStack(this)

        blockEntity?.saveToItem(stack, level.registryAccess())

        stack.set(
            DataComponents.BLOCK_STATE,
            BlockItemStateProperties.EMPTY.with(HONEY_LEVEL, state.getValue(HONEY_LEVEL)),
        )
        return stack
    }

    private fun shouldGiveCreativeStack(state: BlockState, blockEntity: BlockEntity?): Boolean {
        val beeCount = (blockEntity as? BeehiveBlockEntity)?.occupantCount ?: 0
        return hasContents(state.getValue(HONEY_LEVEL), beeCount)
    }

    private fun hasContents(honeyLevel: Int, beeCount: Int): Boolean =
        honeyLevel > 0 || beeCount > 0

    private fun createContentTooltip(labelKey: String, amount: Int, max: Int): Component =
        Component.empty()
            .append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY))
            .append(createContentValueComponent(amount, max))

    private fun createContentValueComponent(amount: Int, max: Int): Component {
        val color = if (amount >= max) ChatFormatting.GREEN else ChatFormatting.GRAY
        return Component.literal("$amount / $max").withStyle(color)
    }

    companion object {
        const val MAX_HONEY_LEVELS = 5

        val HONEY_LEVEL: IntegerProperty = BeehiveBlock.HONEY_LEVEL
        val CODEC: MapCodec<BeehiveBlock> = simpleCodec(::ReinforcedBeehiveBlock)
        private const val HONEY_TOOLTIP = "tooltip.${CreateApiculture.MOD_ID}.reinforced_beehive.honey"
        private const val BEES_TOOLTIP = "tooltip.${CreateApiculture.MOD_ID}.reinforced_beehive.bees"
    }
}
