package me.elephant1214.nogrief.configuration

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.locale.Locale

@Serializable
data class NoGriefConfig(
    var locale: Locale = Locale.EN_US,
    var allowPistonsOutsideOfClaims: Boolean = false,
    var allowPvPInClaims: Boolean = false,
    var fluidsFlowIntoClaims: Boolean = false,
    var initialClaimChunks: Int = 16,
    var maximumPlayerClaimChunks: Int = 128,
    val chunkAcquisitionConfig: ChunkAcquisitionSection = ChunkAcquisitionSection(),
)
