package io.github.reasure3.compat.jade.server

import io.github.reasure3.compat.jade.AFTER_FLUID_STORAGE_PRIORITY
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_BEES_UID
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_BEE_COUNT_TAG
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IServerDataProvider

internal object ReinforcedBeehiveBeeDataProvider : IServerDataProvider<BlockAccessor> {
    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val blockEntity = accessor.blockEntity as? ReinforcedBeehiveBlockEntity ?: return
        data.putInt(REINFORCED_BEEHIVE_BEE_COUNT_TAG, blockEntity.occupantCount)
    }

    override fun getUid(): ResourceLocation =
        REINFORCED_BEEHIVE_BEES_UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
