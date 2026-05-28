package io.github.reasure3.datagen

import io.github.reasure3.datagen.client.CreateApicultureEnUsLanguageProvider
import io.github.reasure3.datagen.client.CreateApicultureKoKrLanguageProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent

object CreateApicultureDataGenerators {
    fun register(modEventBus: IEventBus) {
        modEventBus.addListener(::gatherData)
    }

    private fun gatherData(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput

        generator.addProvider(
            event.includeClient(),
            CreateApicultureEnUsLanguageProvider(output),
        )
        generator.addProvider(
            event.includeClient(),
            CreateApicultureKoKrLanguageProvider(output),
        )
    }
}
