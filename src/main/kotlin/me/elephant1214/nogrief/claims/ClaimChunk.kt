package me.elephant1214.nogrief.claims

import kotlinx.serialization.Serializable
import org.bukkit.Chunk
import org.bukkit.World

@Serializable
data class ClaimChunk(
    val world: World,
    val chunk: Long
) {
    constructor(world: World, x: Int, y: Int) : this(world, Chunk.getChunkKey(x, y))

    fun isSameChunk(claimChunk: ClaimChunk): Boolean = this.world == claimChunk.world && this.chunk == claimChunk.chunk

    fun serialize(): Map<String, Any> = mapOf("chunk" to this.chunk)

    override fun toString(): String = "ClaimChunk(world = $world, chunk = $chunk)"

    companion object {
        fun fromLongSet(world: World, chunks: Set<Long>): MutableSet<ClaimChunk> =
            chunks.map { ClaimChunk(world, it) }.toMutableSet()
    }
}
