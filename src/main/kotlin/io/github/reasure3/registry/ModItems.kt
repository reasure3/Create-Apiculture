package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import net.minecraft.core.Holder
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val REGISTRY: DeferredRegister.Items =
        DeferredRegister.createItems(CreateApiculture.MOD_ID)

    private val creativeTabItems = mutableListOf<DeferredItem<out Item>>()

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }

    fun <T : Item> register(
        name: String,
        factory: () -> T,
    ): DeferredItem<T> =
        track(REGISTRY.register(name, factory))

    internal fun registerSimpleBlockItem(
        block: Holder<Block>,
        properties: Item.Properties = Item.Properties(),
    ): DeferredItem<BlockItem> =
        track(REGISTRY.registerSimpleBlockItem(block, properties))

    internal fun acceptCreativeTabItems(output: CreativeModeTab.Output) {
        creativeTabItems.forEach { output.accept(it.get()) }
    }

    private fun <T : Item> track(item: DeferredItem<T>): DeferredItem<T> {
        creativeTabItems += item
        return item
    }
}
