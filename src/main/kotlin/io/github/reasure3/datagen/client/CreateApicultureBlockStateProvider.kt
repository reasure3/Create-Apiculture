package io.github.reasure3.datagen.client

import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.datagen.blockPath
import io.github.reasure3.datagen.blockTexture
import io.github.reasure3.registry.ModBlocks
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.client.model.generators.ModelFile
import net.neoforged.neoforge.client.model.generators.ModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import net.neoforged.neoforge.registries.DeferredBlock

class CreateApicultureBlockStateProvider(
    output: PackOutput,
    existingFileHelper: ExistingFileHelper,
) : BlockStateProvider(output, CreateApiculture.MOD_ID, existingFileHelper) {
    init {
        existingFileHelper.trackGenerated(CREATE_ANDESITE_CASING_TEXTURE, ModelProvider.TEXTURE)
    }

    override fun registerStatesAndModels() {
        registerReinforcedBeehive()
    }

    private fun registerReinforcedBeehive() {
        val blockEntry = ModBlocks.REINFORCED_BEEHIVE
        val block = blockEntry.get()
        val honeyLevelModels = (0..ReinforcedBeehiveBlock.MAX_HONEY_LEVELS).associateWith { honeyLevel ->
            orientableWithBottomModel(
                blockEntry = blockEntry,
                modelSuffix = "_honey_level_$honeyLevel",
                frontTextureSuffix = "_front_honey_level_$honeyLevel",
                topTexture = CREATE_ANDESITE_CASING_TEXTURE,
            )
        }

        horizontalBlock(block) { state: BlockState ->
            honeyLevelModels.getValue(state.getValue(ReinforcedBeehiveBlock.DISPLAY_HONEY_LEVEL))
        }
        simpleBlockItem(block, honeyLevelModels.getValue(0))
    }

    private fun orientableWithBottomModel(
        blockEntry: DeferredBlock<out Block>,
        modelSuffix: String = "",
        sideTextureSuffix: String = "_side",
        frontTextureSuffix: String = "_front",
        topTexture: ResourceLocation,
        bottomTexture: ResourceLocation = topTexture,
    ): ModelFile {
        val side = blockEntry.blockTexture(sideTextureSuffix)
        return models().orientableWithBottom(
            blockEntry.blockPath(modelSuffix),
            side,
            blockEntry.blockTexture(frontTextureSuffix),
            topTexture,
            bottomTexture,
        ).texture("particle", side)
    }

    companion object {
        private val CREATE_ANDESITE_CASING_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath("create", "block/andesite_casing")
    }
}
