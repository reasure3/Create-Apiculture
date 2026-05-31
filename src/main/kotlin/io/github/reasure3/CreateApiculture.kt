package io.github.reasure3

import com.mojang.logging.LogUtils
import io.github.reasure3.config.CreateApicultureConfig
import io.github.reasure3.content.hive.ReinforcedBeehiveDispenserBehavior
import io.github.reasure3.datagen.CreateApicultureDataGenerators
import io.github.reasure3.registry.ModRegistries
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import org.slf4j.Logger

@Mod(CreateApiculture.MOD_ID)
class CreateApiculture(
    modEventBus: IEventBus,
    modContainer: ModContainer,
) {
    init {
        ModRegistries.register(modEventBus)
        CreateApicultureConfig.register(modContainer)
        CreateApicultureDataGenerators.register(modEventBus)

        modEventBus.addListener(::onCommonSetup)
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        ReinforcedBeehiveDispenserBehavior.register()

        if (CreateApicultureConfig.COMMON.logRegistrySummary.get()) {
            LOGGER.info("Registered base content hooks for {}", MOD_ID)
        }
    }

    companion object {
        const val MOD_ID = "create_apiculture"

        val LOGGER: Logger = LogUtils.getLogger()

        fun id(path: String): ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}
