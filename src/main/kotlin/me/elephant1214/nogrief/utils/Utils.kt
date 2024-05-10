package me.elephant1214.nogrief.utils

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import java.util.*

fun ConfigurationSection.getUuid(path: String): UUID {
    val value = this.getString(path)?.trim()
    if (value.isNullOrEmpty()) {
        error("Expected a UUID at path `$path`, but found null or blank.")
    }

    return try {
        UUID.fromString(value)
    } catch (e: IllegalArgumentException) {
        error("Unable to convert path `$path` to a UUID: ${e.message}")
    }
}

fun UUID.toWorld(): World? = Bukkit.getWorld(this)

fun String.toUuid(): UUID = UUID.fromString(this)

fun horizontalFaces(): Array<BlockFace> = arrayOf(
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
)

fun <T> deserializationTypeError(itemName: String, item: Any?, expected: Class<T>): String =
    "Expected ${expected.simpleName} while deserializing \"$itemName\", should be ${expected.simpleName}, found (${item?.javaClass?.simpleName})"