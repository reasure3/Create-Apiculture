package io.github.reasure3.compat.jade

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import snownee.jade.api.config.IPluginConfig

@WailaPlugin
class CreateApicultureJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlockEntity::class.java)
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlock::class.java)
    }
}

private object ReinforcedBeehiveBeeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    private const val BEE_COUNT_TAG = "BeeCount"
    private const val BEES_TOOLTIP = "tooltip.${CreateApiculture.MOD_ID}.reinforced_beehive.bees"
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

        tooltip.add(createBeeTooltip(serverData.getInt(BEE_COUNT_TAG)), UID)
    }

    override fun appendServerData(data: CompoundTag, accessor: BlockAccessor) {
        val blockEntity = accessor.blockEntity as? ReinforcedBeehiveBlockEntity ?: return
        data.putInt(BEE_COUNT_TAG, blockEntity.occupantCount)
    }

    override fun getUid(): ResourceLocation =
        UID

    private fun createBeeTooltip(beeCount: Int): Component {
        val maxBees = BeehiveBlockEntity.MAX_OCCUPANTS
        val clampedBeeCount = beeCount.coerceIn(0, maxBees)
        val valueColor = if (clampedBeeCount >= maxBees) ChatFormatting.GREEN else ChatFormatting.GRAY

        return Component.empty()
            .append(Component.translatable(BEES_TOOLTIP).withStyle(ChatFormatting.GRAY))
            .append(Component.literal("$clampedBeeCount / $maxBees").withStyle(valueColor))
    }
}
