package io.github.reasure3.datagen

import io.github.reasure3.datagen.client.CreateApicultureEnUsLanguageProvider
import io.github.reasure3.datagen.client.CreateApicultureBlockStateProvider
import io.github.reasure3.datagen.client.CreateApicultureKoKrLanguageProvider
import io.github.reasure3.datagen.server.CreateApicultureBlockTagsProvider
import io.github.reasure3.datagen.server.CreateApicultureLootTableProvider
import io.github.reasure3.datagen.server.CreateApiculturePoiTypeTagsProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent

object CreateApicultureDataGenerators {
    fun register(modEventBus: IEventBus) {
        modEventBus.addListener(::gatherData)
    }

    private fun gatherData(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val existingFileHelper = event.existingFileHelper
        val lookupProvider = event.lookupProvider

        generator.addProvider(
            event.includeClient(),
            CreateApicultureBlockStateProvider(output, existingFileHelper),
        )

        generator.addProvider(
            event.includeClient(),
            CreateApicultureEnUsLanguageProvider(output),
        )
        generator.addProvider(
            event.includeClient(),
            CreateApicultureKoKrLanguageProvider(output),
        )

        generator.addProvider(
            event.includeServer(),
            CreateApicultureBlockTagsProvider(output, lookupProvider, existingFileHelper),
        )
        generator.addProvider(
            event.includeServer(),
            CreateApiculturePoiTypeTagsProvider(output, lookupProvider, existingFileHelper),
        )
        generator.addProvider(
            event.includeServer(),
            CreateApicultureLootTableProvider.create(output, lookupProvider),
        )
    }
}
