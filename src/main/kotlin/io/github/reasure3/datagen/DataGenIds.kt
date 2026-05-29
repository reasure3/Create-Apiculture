package io.github.reasure3.datagen

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock

fun DeferredBlock<out Block>.blockPath(suffix: String = ""): String =
    id.path + suffix

fun DeferredBlock<out Block>.blockTexture(suffix: String = ""): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(id.namespace, "block/${blockPath(suffix)}")

fun DeferredBlock<out Block>.translationKey(): String =
    "block.${id.namespace}.${id.path}"
