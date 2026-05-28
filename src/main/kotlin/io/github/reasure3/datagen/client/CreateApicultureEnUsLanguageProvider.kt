package io.github.reasure3.datagen.client

import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModCreativeTabs
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class CreateApicultureEnUsLanguageProvider(output: PackOutput) :
    LanguageProvider(output, CreateApiculture.MOD_ID, "en_us") {
    override fun addTranslations() {
        add(ModCreativeTabs.MAIN_TAB_TRANSLATION_KEY, "Create: Apiculture")
    }
}
