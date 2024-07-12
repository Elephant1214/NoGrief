package me.elephant1214.nogrief.hooks.pl3xmap

import me.elephant1214.nogrief.claims.Claim

data class MapChunk(
    val minX: Int,
    val minZ: Int,
    val claim: Claim
) {
    fun maxX(): Int = this.minX + 16

    fun maxZ(): Int = this.minZ + 16
    
    fun isTouching(other: MapChunk): Boolean = claim == other.claim && (
            (other.minX == minX && other.minZ == minZ - 1)
                    || (other.minX == minX && other.minZ == minZ + 1)
                    || (other.minX == minX - 1 && other.minZ == minZ)
                    || (other.minX == minX + 1 && other.minZ == minZ)
            )
}
