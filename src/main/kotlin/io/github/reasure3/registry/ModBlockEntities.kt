package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModBlockEntities {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateApiculture.MOD_ID)

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    val REINFORCED_BEEHIVE: DeferredHolder<BlockEntityType<*>, BlockEntityType<ReinforcedBeehiveBlockEntity>> =
        REGISTRY.register("reinforced_beehive", Supplier {
            BlockEntityType.Builder.of(::ReinforcedBeehiveBlockEntity, ModBlocks.REINFORCED_BEEHIVE.get()).build(null)
        })

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }
}
