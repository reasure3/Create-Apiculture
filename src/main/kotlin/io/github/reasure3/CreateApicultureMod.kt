package io.github.reasure3

import com.mojang.logging.LogUtils
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(CreateApiculture.MOD_ID)
@EventBusSubscriber
object CreateApiculture {
    const val MOD_ID = "create_apiculture"

    private val logger: Logger = LogUtils.getLogger()

    init {
        logger.info("Hello from Kotlin + NeoForge")
        MOD_BUS.addListener(::onCommonSetup)
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        logger.info("Common setup from Kotlin")
    }

    @SubscribeEvent
    fun onModBusEvent(event: FMLCommonSetupEvent) {
        logger.info("SubscribeEvent on MOD bus")
    }
}
