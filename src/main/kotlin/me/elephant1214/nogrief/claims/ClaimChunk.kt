package me.elephant1214.nogrief.claims

import kotlinx.serialization.Serializable
import org.bukkit.Chunk
import org.bukkit.World

@Serializable
data class ClaimChunk(
    val world: World,
    val chunk: Chunk
) {
    constructor(chunk: Chunk) : this(chunk.world, chunk)

    constructor(world: World, x: Int, y: Int) : this(world, world.getChunkAt(x, y))

    /**
     * x chunk coordinate
     */
    fun x(): Int = this.chunk.x

    /**
     * y chunk coordinate
     */
    fun z(): Int = this.chunk.z

    /**
     * Lowest x coordinate
     */
    fun minX(): Int = this.chunk.x shl 4

    /**
     * Lowest z coordinate
     */
    fun minZ(): Int = this.chunk.z shl 4

    override fun toString(): String = "ClaimChunk(world = $world, chunk = $chunk)"

    companion object {
        fun fromLongSet(world: World, chunks: Set<Long>): MutableSet<ClaimChunk> =
            chunks.map { ClaimChunk(world, world.getChunkAt(it)) }.toHashSet()
    }
}
