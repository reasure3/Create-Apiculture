package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModPoiTypes {
    val REGISTRY: DeferredRegister<PoiType> =
        DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, CreateApiculture.MOD_ID)

    val REINFORCED_BEEHIVE_KEY: ResourceKey<PoiType> =
        ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, CreateApiculture.id("reinforced_beehive"))

    val REINFORCED_BEEHIVE: DeferredHolder<PoiType, PoiType> =
        REGISTRY.register("reinforced_beehive", Supplier {
            PoiType(allStates(ModBlocks.REINFORCED_BEEHIVE.get()), 0, 1)
        })

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }

    private fun allStates(block: Block): Set<BlockState> =
        block.stateDefinition.possibleStates.toSet()
}
