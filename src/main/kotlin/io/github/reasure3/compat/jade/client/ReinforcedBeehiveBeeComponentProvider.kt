package io.github.reasure3.compat.jade.client

import io.github.reasure3.compat.jade.AFTER_FLUID_STORAGE_PRIORITY
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_BEES_UID
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_BEE_COUNT_TAG
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

internal object ReinforcedBeehiveBeeComponentProvider : IBlockComponentProvider {
    override fun appendTooltip(
        tooltip: ITooltip,
        accessor: BlockAccessor,
        config: IPluginConfig,
    ) {
        val serverData = accessor.serverData
        if (!serverData.contains(REINFORCED_BEEHIVE_BEE_COUNT_TAG)) {
            return
        }

        val beeCount = serverData.getInt(REINFORCED_BEEHIVE_BEE_COUNT_TAG).coerceIn(0, BeehiveBlockEntity.MAX_OCCUPANTS)
        tooltip.add(
            ReinforcedBeehiveBlock.createContentTooltip(
                ReinforcedBeehiveBlock.BEES_TOOLTIP,
                beeCount,
                BeehiveBlockEntity.MAX_OCCUPANTS,
            ),
            REINFORCED_BEEHIVE_BEES_UID,
        )
    }

    override fun getUid(): ResourceLocation =
        REINFORCED_BEEHIVE_BEES_UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
