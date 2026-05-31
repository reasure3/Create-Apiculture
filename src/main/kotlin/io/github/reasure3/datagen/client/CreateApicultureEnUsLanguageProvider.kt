package io.github.reasure3.datagen.client

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.datagen.translationKey
import io.github.reasure3.registry.ModBlocks
import io.github.reasure3.registry.ModCreativeTabs
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class CreateApicultureEnUsLanguageProvider(output: PackOutput) :
    LanguageProvider(output, CreateApiculture.MOD_ID, "en_us") {
    override fun addTranslations() {
        add(ModCreativeTabs.MAIN_TAB_TRANSLATION_KEY, "Create: Apiculture")
        add(ModBlocks.REINFORCED_BEEHIVE.translationKey(), "Reinforced Beehive")
        add(ReinforcedBeehiveBlock.HONEY_TOOLTIP, "Honey: ")
        add(ReinforcedBeehiveBlock.BEES_TOOLTIP, "Bees: ")
        add("config.jade.plugin_${CreateApiculture.MOD_ID}.reinforced_beehive_bees", "Reinforced Beehive Bees")
    }
}
