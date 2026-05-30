package io.github.reasure3.registry

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object ModCapabilities {
    fun register(modEventBus: IEventBus) {
        modEventBus.addListener(::registerCapabilities)
    }

    private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.REINFORCED_BEEHIVE.get(),
        ) { blockEntity, _ -> blockEntity.fluidHandler }
    }
}
