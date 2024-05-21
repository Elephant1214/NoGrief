package me.elephant1214.nogrief.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
@SerialName("chunkAcquisition")
data class ChunkAcquisitionSection(
    var useHourlyChunkAcquisition: Boolean = true,
    var chunksAcquiredPerHour: Int = 2,
    var useChunkPurchasing: Boolean = false,
    var useEconomyForPurchasing: Boolean = false,
    var chunkPurchaseMaterial: Material = Material.DIAMOND,
    var chunkPurchaseCost: Double = 3.0,
)
