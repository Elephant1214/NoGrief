package me.elephant1214.nogrief.players

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.NoGrief

@Serializable
data class PlayerData(
    var remainingClaimChunks: Int = NoGrief.cfg.initialClaimChunks
) {
    fun hasClaimChunks(): Boolean = this.remainingClaimChunks > 0
}
