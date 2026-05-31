package io.github.reasure3.compat.jade

import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import io.github.reasure3.content.hive.ReinforcedBeehiveBlock
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin

@WailaPlugin
class CreateApicultureJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlockEntity::class.java)
        registration.registerEntityDataProvider(ReinforcedBeehiveContraptionProvider, AbstractContraptionEntity::class.java)
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(ReinforcedBeehiveBeeProvider, ReinforcedBeehiveBlock::class.java)
        registration.registerEntityComponent(ReinforcedBeehiveContraptionProvider, AbstractContraptionEntity::class.java)
    }
}
