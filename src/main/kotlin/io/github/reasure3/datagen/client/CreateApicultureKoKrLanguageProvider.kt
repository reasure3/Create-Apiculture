package io.github.reasure3.datagen.client

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.datagen.translationKey
import io.github.reasure3.registry.ModBlocks
import io.github.reasure3.registry.ModCreativeTabs
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class CreateApicultureKoKrLanguageProvider(output: PackOutput) :
    LanguageProvider(output, CreateApiculture.MOD_ID, "ko_kr") {
    override fun addTranslations() {
        add(ModCreativeTabs.MAIN_TAB_TRANSLATION_KEY, "Create: 양봉술")
        add(ModBlocks.REINFORCED_BEEHIVE.translationKey(), "강화 벌통")
        add(ReinforcedBeehiveBlock.HONEY_TOOLTIP, "꿀: ")
        add(ReinforcedBeehiveBlock.BEES_TOOLTIP, "벌: ")
        add("config.jade.plugin_${CreateApiculture.MOD_ID}.reinforced_beehive_bees", "강화 벌통 벌 수")
    }
}
