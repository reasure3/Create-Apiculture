package io.github.reasure3.content.hive

import io.github.reasure3.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class ReinforcedBeehiveBlockEntity(
    pos: BlockPos,
    blockState: BlockState,
) : BeehiveBlockEntity(pos, blockState) {
    override fun getType(): BlockEntityType<*> =
        ModBlockEntities.REINFORCED_BEEHIVE.get()
}
