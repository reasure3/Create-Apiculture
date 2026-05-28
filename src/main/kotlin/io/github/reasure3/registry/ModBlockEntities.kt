package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlockEntities {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateApiculture.MOD_ID)

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }
}
