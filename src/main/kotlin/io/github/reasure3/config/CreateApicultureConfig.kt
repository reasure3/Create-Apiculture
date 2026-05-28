package io.github.reasure3.config

import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue

object CreateApicultureConfig {
    private val commonPair = ModConfigSpec.Builder().configure(::Common)

    val COMMON_SPEC: ModConfigSpec = commonPair.getRight()
    val COMMON: Common = commonPair.getLeft()

    fun register(modContainer: ModContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC)
    }

    class Common(builder: ModConfigSpec.Builder) {
        val logRegistrySummary: BooleanValue = builder
            .comment("Logs a compact registry summary during common setup.")
            .define("logRegistrySummary", false)
    }
}
