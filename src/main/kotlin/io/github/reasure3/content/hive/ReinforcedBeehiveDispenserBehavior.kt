package io.github.reasure3.content.hive

import net.minecraft.core.Direction
import net.minecraft.core.dispenser.BlockSource
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior
import net.minecraft.core.dispenser.DispenseItemBehavior
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.gameevent.GameEvent

object ReinforcedBeehiveDispenserBehavior {
    private var registered = false

    fun register() {
        if (registered) {
            return
        }
        registered = true

        val glassBottleFallback = DispenserBlock.DISPENSER_REGISTRY[Items.GLASS_BOTTLE] ?: DefaultDispenseItemBehavior()
        val shearsFallback = DispenserBlock.DISPENSER_REGISTRY[Items.SHEARS] ?: DefaultDispenseItemBehavior()

        DispenserBlock.registerBehavior(
            Items.GLASS_BOTTLE,
            HoneyBottleDispenseBehavior(glassBottleFallback),
        )
        DispenserBlock.registerBehavior(
            Items.SHEARS,
            ShearsDispenseBehavior(shearsFallback),
        )
    }

    private class HoneyBottleDispenseBehavior(
        private val fallback: DispenseItemBehavior,
    ) : DispenseItemBehavior {
        override fun dispense(blockSource: BlockSource, item: ItemStack): ItemStack {
            val hive = reinforcedHiveTarget(blockSource) ?: return fallback.dispense(blockSource, item)
            if (!hive.blockEntity.tryConsumeManualUnit()) {
                return fallback.dispense(blockSource, item)
            }

            hive.releaseBees()
            hive.level.gameEvent(null, GameEvent.FLUID_PICKUP, hive.pos)
            val result = consumeWithRemainder(blockSource, item, ItemStack(Items.HONEY_BOTTLE))
            playDispenseResult(blockSource, success = true)
            return result
        }
    }

    private class ShearsDispenseBehavior(
        private val fallback: DispenseItemBehavior,
    ) : DispenseItemBehavior {
        override fun dispense(blockSource: BlockSource, item: ItemStack): ItemStack {
            val hive = reinforcedHiveTarget(blockSource) ?: return fallback.dispense(blockSource, item)
            if (!hive.blockEntity.tryConsumeManualUnit()) {
                return fallback.dispense(blockSource, item)
            }

            hive.level.playSound(null, hive.pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f)
            Block.popResource(hive.level, hive.pos, ItemStack(Items.HONEYCOMB, SHEARED_HONEYCOMB_COUNT))
            hive.releaseBees()
            hive.level.gameEvent(null, GameEvent.SHEAR, hive.pos)
            item.hurtAndBreak(1, hive.level, null as LivingEntity?) {}
            playDispenseResult(blockSource, success = true)
            return item
        }
    }

    private data class HiveTarget(
        val level: ServerLevel,
        val pos: net.minecraft.core.BlockPos,
        val state: net.minecraft.world.level.block.state.BlockState,
        val blockEntity: ReinforcedBeehiveBlockEntity,
    ) {
        fun releaseBees() {
            blockEntity.emptyAllLivingFromHive(null, state, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED)
        }
    }

    private fun reinforcedHiveTarget(blockSource: BlockSource): HiveTarget? {
        val level = blockSource.level()
        val pos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING))
        val state = level.getBlockState(pos)
        val blockEntity = level.getBlockEntity(pos) as? ReinforcedBeehiveBlockEntity ?: return null
        return HiveTarget(level, pos, state, blockEntity)
    }

    private fun consumeWithRemainder(
        blockSource: BlockSource,
        stack: ItemStack,
        remainder: ItemStack,
    ): ItemStack {
        stack.shrink(1)
        if (stack.isEmpty) {
            return remainder
        }

        val remainingRemainder = blockSource.blockEntity().insertItem(remainder)
        if (!remainingRemainder.isEmpty) {
            dispenseRemainder(blockSource, remainingRemainder)
        }
        return stack
    }

    private fun dispenseRemainder(blockSource: BlockSource, stack: ItemStack) {
        val direction = blockSource.state().getValue(DispenserBlock.FACING)
        DefaultDispenseItemBehavior.spawnItem(
            blockSource.level(),
            stack,
            DEFAULT_DISPENSE_ACCURACY,
            direction,
            DispenserBlock.getDispensePosition(blockSource),
        )
        playDispenseResult(blockSource, success = true)
    }

    private fun playDispenseResult(blockSource: BlockSource, success: Boolean) {
        val direction: Direction = blockSource.state().getValue(DispenserBlock.FACING)
        blockSource.level().levelEvent(if (success) SUCCESS_DISPENSE_EVENT else FAILED_DISPENSE_EVENT, blockSource.pos(), 0)
        blockSource.level().levelEvent(DISPENSE_ANIMATION_EVENT, blockSource.pos(), direction.get3DDataValue())
    }

    private const val SHEARED_HONEYCOMB_COUNT = 3
    private const val DEFAULT_DISPENSE_ACCURACY = 6
    private const val SUCCESS_DISPENSE_EVENT = 1000
    private const val FAILED_DISPENSE_EVENT = 1001
    private const val DISPENSE_ANIMATION_EVENT = 2000
}
