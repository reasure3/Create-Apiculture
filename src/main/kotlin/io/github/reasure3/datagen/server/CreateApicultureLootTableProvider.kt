package io.github.reasure3.datagen.server

import io.github.reasure3.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture

object CreateApicultureLootTableProvider {
    fun create(
        output: PackOutput,
        lookupProvider: CompletableFuture<HolderLookup.Provider>,
    ): LootTableProvider =
        LootTableProvider(
            output,
            emptySet(),
            listOf(
                LootTableProvider.SubProviderEntry(::BlockLootProvider, LootContextParamSets.BLOCK),
            ),
            lookupProvider,
        )

    private class BlockLootProvider(registries: HolderLookup.Provider) :
        BlockLootSubProvider(emptySet<Item>(), FeatureFlags.REGISTRY.allFlags(), registries) {
        override fun generate() {
            add(ModBlocks.REINFORCED_BEEHIVE.get(), ::createBeeHiveDrop)
        }

        override fun getKnownBlocks(): Iterable<Block> =
            listOf(ModBlocks.REINFORCED_BEEHIVE.get())
    }
}
