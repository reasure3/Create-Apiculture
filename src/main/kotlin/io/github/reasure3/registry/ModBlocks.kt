package io.github.reasure3.registry

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks {
    val REGISTRY: DeferredRegister.Blocks =
        DeferredRegister.createBlocks(CreateApiculture.MOD_ID)

    val REINFORCED_BEEHIVE: DeferredBlock<ReinforcedBeehiveBlock> =
        registerWithItem(
            name = "reinforced_beehive",
            properties = andesiteCasingProperties(),
            factory = ::ReinforcedBeehiveBlock,
        )

    fun register(modEventBus: IEventBus) {
        REGISTRY.register(modEventBus)
    }

    fun <T : Block> registerWithItem(
        name: String,
        properties: BlockBehaviour.Properties,
        itemProperties: Item.Properties = Item.Properties(),
        factory: (BlockBehaviour.Properties) -> T,
    ): DeferredBlock<T> {
        val block = REGISTRY.registerBlock(name, factory, properties)
        ModItems.registerSimpleBlockItem(block, itemProperties)
        return block
    }

    private fun andesiteCasingProperties(): BlockBehaviour.Properties =
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
            .mapColor(MapColor.PODZOL)
            .sound(SoundType.WOOD)
}
