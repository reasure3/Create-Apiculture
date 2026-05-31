package io.github.reasure3.compat.jade

import io.github.reasure3.CreateApiculture
import net.minecraft.resources.ResourceLocation

internal const val AFTER_FLUID_STORAGE_PRIORITY = 1001

internal const val REINFORCED_BEEHIVE_BEE_COUNT_TAG = "BeeCount"
internal val REINFORCED_BEEHIVE_BEES_UID: ResourceLocation =
    CreateApiculture.id("reinforced_beehive_bees")

internal const val REINFORCED_BEEHIVE_HIVE_COUNT_TAG = "ReinforcedBeehiveCount"
internal const val REINFORCED_BEEHIVE_HONEY_AMOUNT_TAG = "ReinforcedBeehiveHoneyAmount"
internal const val REINFORCED_BEEHIVE_CONTRAPTION_BEE_COUNT_TAG = "ReinforcedBeehiveBeeCount"
internal const val VANILLA_BEEHIVE_BEES_TAG = "bees"
internal val REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID: ResourceLocation =
    CreateApiculture.id("reinforced_beehive_contraption_contents")
