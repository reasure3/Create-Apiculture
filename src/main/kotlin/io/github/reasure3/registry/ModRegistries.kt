package io.github.reasure3.registry

import net.neoforged.bus.api.IEventBus

object ModRegistries {
    fun register(modEventBus: IEventBus) {
        ModBlocks.register(modEventBus)
        ModItems.register(modEventBus)
        ModBlockEntities.register(modEventBus)
        ModCreativeTabs.register(modEventBus)
    }
}
