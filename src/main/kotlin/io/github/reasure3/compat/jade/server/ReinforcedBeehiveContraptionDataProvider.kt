package io.github.reasure3.compat.jade.server

import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import io.github.reasure3.compat.jade.AFTER_FLUID_STORAGE_PRIORITY
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_CONTRAPTION_BEE_COUNT_TAG
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_HIVE_COUNT_TAG
import io.github.reasure3.compat.jade.REINFORCED_BEEHIVE_HONEY_AMOUNT_TAG
import io.github.reasure3.compat.jade.VANILLA_BEEHIVE_BEES_TAG
import io.github.reasure3.content.hive.ReinforcedBeehiveBlockEntity
import io.github.reasure3.registry.ModBlocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IServerDataProvider

internal object ReinforcedBeehiveContraptionDataProvider : IServerDataProvider<EntityAccessor> {
    override fun appendServerData(data: CompoundTag, accessor: EntityAccessor) {
        val contraptionEntity = accessor.entity as? AbstractContraptionEntity ?: return
        val contraption = contraptionEntity.getContraption() ?: return

        var hiveCount = 0
        var honeyAmount = 0
        var beeCount = 0
        for (blockInfo in contraption.getBlocks().values) {
            if (blockInfo.state().block != ModBlocks.REINFORCED_BEEHIVE.get()) {
                continue
            }

            hiveCount += 1
            val nbt = blockInfo.nbt() ?: continue
            honeyAmount += ReinforcedBeehiveBlockEntity.readStoredHoneyMb(nbt)
            beeCount += nbt.getList(VANILLA_BEEHIVE_BEES_TAG, Tag.TAG_COMPOUND.toInt()).size
        }

        if (hiveCount <= 0) {
            return
        }

        data.putInt(REINFORCED_BEEHIVE_HIVE_COUNT_TAG, hiveCount)
        data.putInt(REINFORCED_BEEHIVE_HONEY_AMOUNT_TAG, honeyAmount)
        data.putInt(REINFORCED_BEEHIVE_CONTRAPTION_BEE_COUNT_TAG, beeCount)
    }

    override fun getUid(): ResourceLocation =
        REINFORCED_BEEHIVE_CONTRAPTION_CONTENTS_UID

    override fun getDefaultPriority(): Int =
        AFTER_FLUID_STORAGE_PRIORITY
}
