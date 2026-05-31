package io.github.reasure3.compat.jade

import com.simibubi.create.AllFluids
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import io.github.reasure3.registry.ModBlocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IEntityComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.fluid.JadeFluidObject
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.BoxStyle
import snownee.jade.api.ui.IDisplayHelper
import snownee.jade.api.ui.IElementHelper
import snownee.jade.api.view.FluidView

@WailaPlugin
class CreateApicultureJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlockEntity::class.java)
        registration.registerEntityDataProvider(ReinforcedBeehiveContraptionProvider, AbstractContraptionEntity::class.java)
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlock::class.java)
        registration.registerEntityComponent(ReinforcedBeehiveContraptionProvider, AbstractContraptionEntity::class.java)
    }
}

private const val AFTER_FLUID_STORAGE_PRIORITY = 1001

private object ReinforcedBeehiveBeeProvider : IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    private const val BEE_COUNT_TAG = "BeeCount"
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

private object ReinforcedBeehiveContraptionProvider : IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    private const val HIVE_COUNT_TAG = "ReinforcedBeehiveCount"
    private const val HONEY_AMOUNT_TAG = "ReinforcedBeehiveHoneyAmount"
    private const val BEE_COUNT_TAG = "ReinforcedBeehiveBeeCount"
    private const val BEES_TAG = "bees"
    private val UID: ResourceLocation = CreateApiculture.id("reinforced_beehive_contraption_contents")

    override fun appendTooltip(
        tooltip: ITooltip,
        accessor: EntityAccessor,
        config: IPluginConfig,
    ) {
        val serverData = accessor.serverData
        if (!serverData.contains(HIVE_COUNT_TAG)) {
            return
        }

        val hiveCount = serverData.getInt(HIVE_COUNT_TAG)
        if (hiveCount <= 0) {
            return
        }

        val honeyCapacity = hiveCount * ReinforcedBeehiveBlockEntity.HONEY_CAPACITY_MB
        val beeCapacity = hiveCount * BeehiveBlockEntity.MAX_OCCUPANTS
        val honeyAmount = serverData.getInt(HONEY_AMOUNT_TAG).coerceIn(0, honeyCapacity)
        val beeCount = serverData.getInt(BEE_COUNT_TAG).coerceIn(0, beeCapacity)

        appendHoneyTooltip(tooltip, accessor, honeyAmount, honeyCapacity)
        tooltip.add(
            ReinforcedBeehiveBlock.createContentTooltip(
                ReinforcedBeehiveBlock.BEES_TOOLTIP,
                beeCount,
                beeCapacity,
            ),
            UID,
        )
    }

    private fun appendHoneyTooltip(
        tooltip: ITooltip,
        accessor: EntityAccessor,
        honeyAmount: Int,
        honeyCapacity: Int,
    ) {
        val helper = IElementHelper.get()
        val honey = JadeFluidObject.of(AllFluids.HONEY.get(), honeyAmount.toLong())
        val fluidView = FluidView.readDefault(FluidView.writeDefault(honey, honeyCapacity.toLong())) ?: return

        tooltip.add(
            helper.progress(
                fluidView.ratio,
                createHoneyBarText(accessor, fluidView),
                helper.progressStyle().overlay(fluidView.overlay),
                BoxStyle.getNestedBox(),
                true,
            ).tag(UID),
        )
    }

    private fun createHoneyBarText(accessor: EntityAccessor, fluidView: FluidView): Component {
        // Detail mode builds the amount part as "1.25B / 8B"; normal mode keeps it as "1.25B".
        val amountText = if (accessor.showDetails()) {
            Component.translatable(
                "jade.fluid.with_capacity",
                IThemeHelper.get().info(fluidView.current),
                fluidView.max,
            )
        } else {
            IThemeHelper.get().info(fluidView.current)
        }
        // The outer Jade fluid label combines the fluid name and amount part, e.g. "Honey 1.25B".
        return Component.translatable(
            "jade.fluid",
            IThemeHelper.get().info(IDisplayHelper.get().stripColor(fluidView.fluidName)),
            amountText,
        )
    }

    override fun appendServerData(data: CompoundTag, accessor: EntityAccessor) {
        val contraptionEntity = accessor.entity as? AbstractContraptionEntity ?: return
        val contraption = contraptionEntity.getContraption() ?: return

        var hiveCount = 0
        var honeyAmount = 0
        var beeCount = 0
        for (blockInfo in contraption.getBlocks().values) {
            if (blockInfo.state().block != ModBlocks.REINFORCED_BEEHIVE.get()) {
                continue
            }

            hiveCount += 1
            val nbt = blockInfo.nbt() ?: continue
            honeyAmount += ReinforcedBeehiveBlockEntity.readStoredHoneyMb(nbt)
            beeCount += nbt.getList(BEES_TAG, Tag.TAG_COMPOUND.toInt()).size
        }

        if (hiveCount <= 0) {
            return
        }

        data.putInt(HIVE_COUNT_TAG, hiveCount)
        data.putInt(HONEY_AMOUNT_TAG, honeyAmount)
        data.putInt(BEE_COUNT_TAG, beeCount)
    }

    override fun getUid(): ResourceLocation =
        UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
