package me.elephant1214.nogrief.players

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.NoGrief

@Serializable
data class PlayerData(val remainingClaimChunks: Int = NoGrief.cfg.initialClaimChunks)
