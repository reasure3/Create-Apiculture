package io.github.reasure3.datagen.server

import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CreateApicultureBlockTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper,
) : BlockTagsProvider(output, lookupProvider, CreateApiculture.MOD_ID, existingFileHelper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.BEEHIVES).add(ModBlocks.REINFORCED_BEEHIVE.get())
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.REINFORCED_BEEHIVE.get())
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.REINFORCED_BEEHIVE.get())
    }
}
