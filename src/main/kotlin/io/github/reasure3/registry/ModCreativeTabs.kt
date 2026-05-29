package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModCreativeTabs {
    const val MAIN_TAB_TRANSLATION_KEY = "itemGroup.${CreateApiculture.MOD_ID}"

    private val REGISTRY: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateApiculture.MOD_ID)

    val MAIN: DeferredHolder<CreativeModeTab, CreativeModeTab> = REGISTRY.register("main", Supplier {
        CreativeModeTab.builder()
            .title(Component.translatable(MAIN_TAB_TRANSLATION_KEY))
            .icon { ItemStack(Items.HONEYCOMB) }
            .displayItems { _, output -> ModItems.acceptCreativeTabItems(output) }
            .build()
    })

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }
}
