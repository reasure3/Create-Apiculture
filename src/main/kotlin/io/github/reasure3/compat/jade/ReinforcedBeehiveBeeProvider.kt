package io.github.reasure3.compat.jade

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

internal object ReinforcedBeehiveBeeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    private const val BEE_COUNT_TAG = "BeeCount"
    private const val AFTER_FLUID_STORAGE_PRIORITY = 1001
    private val UID: ResourceLocation = CreateApiculture.id("reinforced_beehive_bees")

    override fun appendTooltip(
        tooltip: ITooltip,
        accessor: BlockAccessor,
        config: IPluginConfig,
    ) {
        val serverData = accessor.serverData
        if (!serverData.contains(BEE_COUNT_TAG)) {
            return
        }

        val beeCount = serverData.getInt(BEE_COUNT_TAG).coerceIn(0, BeehiveBlockEntity.MAX_OCCUPANTS)
        tooltip.add(
            ReinforcedBeehiveBlock.createContentTooltip(
                ReinforcedBeehiveBlock.BEES_TOOLTIP,
                beeCount,
                BeehiveBlockEntity.MAX_OCCUPANTS,
            ),
            UID,
        )
    }

    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val blockEntity = accessor.blockEntity as? ReinforcedBeehiveBlockEntity ?: return
        data.putInt(BEE_COUNT_TAG, blockEntity.occupantCount)
    }

    override fun getUid(): ResourceLocation =
        UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
