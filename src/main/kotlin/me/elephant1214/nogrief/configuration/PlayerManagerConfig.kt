package me.elephant1214.nogrief.configuration

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.utils.UUIDSerializer
import java.util.UUID

@Serializable
internal data class PlayerManagerConfig(
    val bypassPlayers: MutableSet<@Serializable(UUIDSerializer::class) UUID> = mutableSetOf()
)
