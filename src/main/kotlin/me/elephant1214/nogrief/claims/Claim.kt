package me.elephant1214.nogrief.claims

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.utils.getUuid
import me.elephant1214.nogrief.utils.toWorld
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

class Claim(
    val claimId: UUID = ClaimManager.makeNewClaimID(),
    ownerIn: UUID,
    val world: World,
    private var chunkCount: Int,
    private val permissions: MutableMap<UUID, ClaimPermissionLevel> = mutableMapOf(ownerIn to ClaimPermissionLevel.MANAGER),
    private var modifiedAt: Instant = Instant.now()
) {
    var owner: UUID = ownerIn
        set(value) {
            field = value
            setPermissionLevel(value, ClaimPermissionLevel.MANAGER)
        }

    constructor(owner: UUID, world: World, chunk: ClaimChunk) : this(
        ownerIn = owner,
        world = world,
        chunkCount = 1,
    ) {
        ClaimManager.addChunk(chunk, this)
    }

    fun canModifyBlocks(player: Player): Boolean =
        this.getPermissionLevel(player.uniqueId) != ClaimPermissionLevel.VISITOR

    fun canModifyContainers(player: Player): Boolean =
        this.getPermissionLevel(player.uniqueId).ordinal >= ClaimPermissionLevel.CONTAINER_TRUSTED.ordinal

    fun getPermissionLevel(player: UUID): ClaimPermissionLevel =
        this.permissions[player] ?: ClaimPermissionLevel.VISITOR

    fun setPermissionLevel(player: UUID, level: ClaimPermissionLevel) {
        this.permissions[player] = level
        this.markModified()
    }

    fun containsChunk(chunk: ClaimChunk): Boolean {
        val claim = ClaimManager.getClaim(chunk)
        return claim != null && claim == this
    }

    fun containsChunk(chunk: Chunk): Boolean = this.containsChunk(ClaimChunk(chunk.world, chunk.chunkKey))

    fun getChunks(): List<ClaimChunk> = ClaimManager.getChunksForClaim(this)

    fun addChunk(chunk: ClaimChunk): ClaimChunkAddResult {
        if (chunk.world != this.world) return ClaimChunkAddResult.FAILED_WRONG_WORLD
        if (ClaimManager.getClaim(chunk) == null) {
            ClaimManager.addChunk(chunk, this)
            this.chunkCount++
            this.markModified()
            return ClaimChunkAddResult.SUCCESS
        } else {
            return ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED
        }
    }

    fun removeChunk(chunk: ClaimChunk) {
        ClaimManager.removeChunk(chunk)
        this.chunkCount--
        this.markModified()
    }

    private fun markModified() {
        this.modifiedAt = Instant.now()
    }

    fun serialize(): Map<String, Any> = mapOf(
        "claimId" to this.claimId,
        "owner" to this.owner,
        "world" to this.world.uid,
        "chunks" to this.getChunks(),
    )

    private fun loadChunks(chunks: List<ClaimChunk>): ClaimChunkAddResult {
        chunks.forEach { chunk ->
            // Shouldn't be possible, but check anyway
            if (chunk.world != this.world) return@loadChunks ClaimChunkAddResult.FAILED_WRONG_WORLD
            if (ClaimManager.getClaim(chunk) == null) {
                ClaimManager.addChunk(chunk, this)
                this.chunkCount++
            } else {
                return@loadChunks ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED
            }
        }
        this.markModified()
        return ClaimChunkAddResult.SUCCESS
    }

    companion object {
        fun fromYml(claimId: UUID, yml: ConfigurationSection): Claim {
            val storedId = yml.getUuid("id")
            if (storedId != claimId) {
                error("Requested and stored claim IDs do not match ($claimId, $storedId)! Please fix this manually.")
            }
            val owner = yml.getUuid("owner")
            val world = yml.getUuid("world").toWorld()
            world?.let {
                // Expected chunk count
                var chunkCount = yml.getInt("chunkCount")
                val chunks = yml.getLongList("chunks")
                // This means that getLongList was unable to convert some values in the list at `chunks` to longs
                if (chunks.size != chunkCount) {
                    NoGrief.logger.warning("Expected $chunkCount in claim $claimId, only found ${chunks.size}. Changing claim chunk count to match.")
                    chunkCount = chunks.size
                }

                val claim = Claim(claimId, owner, world, chunkCount)
                val result = claim.loadChunks(chunks.map { ClaimChunk(world, it) })
                if (result != ClaimChunkAddResult.SUCCESS) {
                    ClaimManager.removeClaim(claim)
                    error("A chunk was not able to be added to a loaded claim. Reason: $result")
                }
                return claim
            } ?: run {
                error("Could not find world specified by claim $claimId")
            }
        }
    }
}
