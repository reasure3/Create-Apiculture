package io.github.reasure3.compat.jade

import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import io.github.reasure3.compat.jade.client.ReinforcedBeehiveBeeComponentProvider
import io.github.reasure3.compat.jade.client.ReinforcedBeehiveContraptionComponentProvider
import io.github.reasure3.compat.jade.server.ReinforcedBeehiveBeeDataProvider
import io.github.reasure3.compat.jade.server.ReinforcedBeehiveContraptionDataProvider
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin

@WailaPlugin
class CreateApicultureJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(ReinforcedBeehiveBeeDataProvider, ReinforcedBeehiveBlockEntity::class.java)
        registration.registerEntityDataProvider(ReinforcedBeehiveContraptionDataProvider, AbstractContraptionEntity::class.java)
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(ReinforcedBeehiveBeeComponentProvider, ReinforcedBeehiveBlock::class.java)
        registration.registerEntityComponent(ReinforcedBeehiveContraptionComponentProvider, AbstractContraptionEntity::class.java)
    }
}
