package io.github.reasure3.content.hive

import com.mojang.serialization.MapCodec
import com.simibubi.create.content.equipment.wrench.IWrenchable
import io.github.reasure3.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty

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

    companion object {
        const val MAX_HONEY_LEVELS = 5

        val HONEY_LEVEL: IntegerProperty = BeehiveBlock.HONEY_LEVEL
        val CODEC: MapCodec<BeehiveBlock> = simpleCodec(::ReinforcedBeehiveBlock)
    }
}
