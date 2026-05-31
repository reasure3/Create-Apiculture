package io.github.reasure3.compat.jade.client

import com.simibubi.create.AllFluids
import io.github.reasure3.compat.jade.AFTER_FLUID_STORAGE_PRIORITY
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_CONTRAPTION_BEE_COUNT_TAG
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_HIVE_COUNT_TAG
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_HONEY_AMOUNT_TAG
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IEntityComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.fluid.JadeFluidObject
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.BoxStyle
import snownee.jade.api.ui.IDisplayHelper
import snownee.jade.api.ui.IElementHelper
import snownee.jade.api.view.FluidView

internal object ReinforcedBeehiveContraptionComponentProvider : IEntityComponentProvider {
    override fun appendTooltip(
        tooltip: ITooltip,
        accessor: EntityAccessor,
        config: IPluginConfig,
    ) {
        val serverData = accessor.serverData
        if (!serverData.contains(REINFORCED_BEEHIVE_HIVE_COUNT_TAG)) {
            return
        }

        val hiveCount = serverData.getInt(REINFORCED_BEEHIVE_HIVE_COUNT_TAG)
        if (hiveCount <= 0) {
            return
        }

        val honeyCapacity = hiveCount * ReinforcedBeehiveBlockEntity.HONEY_CAPACITY_MB
        val beeCapacity = hiveCount * BeehiveBlockEntity.MAX_OCCUPANTS
        val honeyAmount = serverData.getInt(REINFORCED_BEEHIVE_HONEY_AMOUNT_TAG).coerceIn(0, honeyCapacity)
        val beeCount = serverData.getInt(REINFORCED_BEEHIVE_CONTRAPTION_BEE_COUNT_TAG).coerceIn(0, beeCapacity)

        appendHoneyTooltip(tooltip, accessor, honeyAmount, honeyCapacity)
        tooltip.add(
            ReinforcedBeehiveBlock.createContentTooltip(
                ReinforcedBeehiveBlock.BEES_TOOLTIP,
                beeCount,
                beeCapacity,
            ),
            REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID,
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
            ).tag(REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID),
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

    override fun getUid(): ResourceLocation =
        REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
