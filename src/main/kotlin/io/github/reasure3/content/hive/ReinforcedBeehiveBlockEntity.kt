package io.github.reasure3.content.hive

import com.simibubi.create.AllFluids
import io.github.reasure3.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import net.neoforged.neoforge.fluids.capability.templates.FluidTank
import kotlin.math.floor

class ReinforcedBeehiveBlockEntity(
    pos: BlockPos,
    blockState: BlockState,
) : BeehiveBlockEntity(pos, blockState) {
    private val honeyTank = object : FluidTank(HONEY_CAPACITY_MB, ::isValidHoney) {
        override fun onContentsChanged() {
            this@ReinforcedBeehiveBlockEntity.onHoneyContentsChanged()
        }
    }

    val fluidHandler: IFluidHandler
        get() = honeyTank

    val storedHoneyMb: Int
        get() = honeyTank.fluidAmount

    // Carries fractional mB between deliveries so production multipliers do not discard remainder.
    private var productionRemainder: Double = 0.0
    // Defers derived display_honey_level repair until the first server tick after NBT load.
    private var displayStateSyncPending = true

    override fun getType(): BlockEntityType<*> =
        ModBlockEntities.REINFORCED_BEEHIVE.get()

    fun tryConsumeManualUnit(): Boolean {
        if (storedHoneyMb < MANUAL_HONEY_UNIT_MB) {
            return false
        }

        honeyTank.drain(MANUAL_HONEY_UNIT_MB, FluidAction.EXECUTE)
        return true
    }

    fun setStoredHoneyMb(amount: Int) {
        val clampedAmount = amount.coerceIn(0, HONEY_CAPACITY_MB)
        honeyTank.setFluid(createHoneyStack(clampedAmount))
        onHoneyContentsChanged()
    }

    fun syncDisplayState(updateFlags: Int = Block.UPDATE_ALL) {
        val level = level ?: return
        if (level.isClientSide) {
            return
        }
        displayStateSyncPending = false

        val state = level.getBlockState(worldPosition)
        val displayLevel = ReinforcedBeehiveBlock.displayHoneyLevel(storedHoneyMb)
        if (state.getValue(ReinforcedBeehiveBlock.DISPLAY_HONEY_LEVEL) == displayLevel) {
            return
        }

        level.setBlock(
            worldPosition,
            state.setValue(ReinforcedBeehiveBlock.DISPLAY_HONEY_LEVEL, displayLevel),
            updateFlags,
        )
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        productionRemainder = tag.getDouble(PRODUCTION_REMAINDER_TAG)
        honeyTank.setFluid(createHoneyStack(readStoredHoneyMb(tag)))
        displayStateSyncPending = true
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        writeHoneyData(tag, storedHoneyMb, productionRemainder)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> =
        ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag =
        saveCustomOnly(registries)

    private fun addHoneyFromDeliveries(deliveryCount: Int) {
        if (deliveryCount <= 0) {
            return
        }

        val producedHoneyMb = BASE_HONEY_PER_DELIVERY_MB * effectiveProductionMultiplier() * deliveryCount
        if (producedHoneyMb <= 0.0) {
            return
        }

        val previousRemainder = productionRemainder
        val totalProduction = productionRemainder + producedHoneyMb
        val wholeMb = floor(totalProduction).toInt()
        productionRemainder = totalProduction - wholeMb

        val fillableMb = wholeMb.coerceAtMost(HONEY_CAPACITY_MB - storedHoneyMb)
        if (fillableMb > 0) {
            honeyTank.fill(createHoneyStack(fillableMb), FluidAction.EXECUTE)
        } else if (productionRemainder != previousRemainder) {
            setChanged()
        }
    }

    private fun effectiveProductionMultiplier(): Double =
        productionMultiplier(BASELINE_HAPPINESS)

    private fun onHoneyContentsChanged() {
        setChanged()
        syncDisplayState()
    }

    private fun nectarBeeCount(): Int =
        stored.count { it.hasNectar() }

    companion object {
        const val HONEY_CAPACITY_MB = 1250
        const val MANUAL_HONEY_UNIT_MB = 250
        const val VANILLA_HONEY_LEVEL_MB = 50
        private const val BASE_HONEY_PER_DELIVERY_MB = 50
        private const val BASELINE_HAPPINESS = 30

        const val STORED_HONEY_MB_TAG = "stored_honey_mb"
        private const val PRODUCTION_REMAINDER_TAG = "production_remainder"
        fun serverTick(
            level: Level,
            pos: BlockPos,
            state: BlockState,
            blockEntity: ReinforcedBeehiveBlockEntity,
        ) {
            if (blockEntity.displayStateSyncPending) {
                blockEntity.syncDisplayState()
            }

            if (blockEntity.isEmpty) {
                return
            }

            val nectarBeesBefore = blockEntity.nectarBeeCount()
            BeehiveBlockEntity.serverTick(level, pos, state, blockEntity)
            val deliveredNectarBees = nectarBeesBefore - blockEntity.nectarBeeCount()

            blockEntity.addHoneyFromDeliveries(deliveredNectarBees)
        }

        fun readStoredHoneyMb(tag: CompoundTag): Int =
            tag.getInt(STORED_HONEY_MB_TAG).coerceIn(0, HONEY_CAPACITY_MB)

        fun writeHoneyData(
            tag: CompoundTag,
            storedHoneyMb: Int,
            productionRemainder: Double = 0.0,
        ): CompoundTag {
            val clampedStoredHoneyMb = storedHoneyMb.coerceIn(0, HONEY_CAPACITY_MB)
            if (clampedStoredHoneyMb > 0) {
                tag.putInt(STORED_HONEY_MB_TAG, clampedStoredHoneyMb)
            } else {
                tag.remove(STORED_HONEY_MB_TAG)
            }

            val clampedRemainder = Mth.clamp(productionRemainder, 0.0, 1.0)
            if (clampedRemainder > 0.0) {
                tag.putDouble(PRODUCTION_REMAINDER_TAG, clampedRemainder)
            } else {
                tag.remove(PRODUCTION_REMAINDER_TAG)
            }
            return tag
        }

        fun productionMultiplier(happiness: Int): Double {
            val clampedHappiness = happiness.coerceIn(0, 100)
            return when {
                clampedHappiness == 0 -> 0.0
                clampedHappiness <= 30 -> {
                    val t = clampedHappiness / 30.0
                    t * (0.75 + 0.25 * t)
                }
                clampedHappiness <= 70 -> {
                    val t = (clampedHappiness - 30) / 40.0
                    1.0 + 1.5 * t
                }
                else -> {
                    val t = (clampedHappiness - 70) / 30.0
                    2.5 + 2.5 * t * t * t
                }
            }
        }

        private fun isValidHoney(stack: FluidStack): Boolean =
            stack.`is`(AllFluids.HONEY.get())

        private fun createHoneyStack(amount: Int): FluidStack =
            if (amount > 0) {
                FluidStack(AllFluids.HONEY.get(), amount)
            } else {
                FluidStack.EMPTY
            }
    }
}
