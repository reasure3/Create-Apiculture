package io.github.reasure3.datagen.client

import io.github.reasure3.CreateApiculture
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
    }
}
