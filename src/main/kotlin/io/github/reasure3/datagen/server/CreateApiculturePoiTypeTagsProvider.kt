package io.github.reasure3.datagen.server

import io.github.reasure3.CreateApiculture
import io.github.reasure3.registry.ModPoiTypes
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.TagsProvider
import net.minecraft.tags.PoiTypeTags
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CreateApiculturePoiTypeTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper,
) : TagsProvider<PoiType>(
    output,
    Registries.POINT_OF_INTEREST_TYPE,
    lookupProvider,
    CreateApiculture.MOD_ID,
    existingFileHelper,
) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(PoiTypeTags.BEE_HOME).add(ModPoiTypes.REINFORCED_BEEHIVE_KEY)
    }
}
