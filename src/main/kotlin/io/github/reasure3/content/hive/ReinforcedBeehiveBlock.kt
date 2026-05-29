package io.github.reasure3.content.hive

import com.mojang.serialization.MapCodec
import com.simibubi.create.content.equipment.wrench.IWrenchable
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

class ReinforcedBeehiveBlock(properties: Properties) : HorizontalDirectionalBlock(properties), IWrenchable {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HONEY_LEVEL, 0),
        )
    }

    override fun codec(): MapCodec<out HorizontalDirectionalBlock> = CODEC

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)

    override fun getRotatedBlockState(originalState: BlockState, targetedFace: Direction): BlockState =
        if (targetedFace.axis == Direction.Axis.Y) {
            originalState.setValue(FACING, originalState.getValue(FACING).getClockWise(Direction.Axis.Y))
        } else {
            originalState
        }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, HONEY_LEVEL)
    }

    companion object {
        const val MAX_HONEY_LEVELS = 5

        val HONEY_LEVEL: IntegerProperty = BlockStateProperties.LEVEL_HONEY
        val CODEC: MapCodec<ReinforcedBeehiveBlock> = simpleCodec(::ReinforcedBeehiveBlock)
    }
}
