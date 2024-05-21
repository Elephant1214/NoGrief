package me.elephant1214.nogrief.configuration

import kotlinx.serialization.Serializable

@Serializable
data class NoGriefConfig(
    var allowPistonsOutsideOfClaims: Boolean = false,
    var initialClaimChunks: Int = 16,
    var maximumPlayerClaimChunks: Int = 128,
    val chunkAcquisitionConfig: ChunkAcquisitionSection = ChunkAcquisitionSection(),
)