package me.elephant1214.nogrief.claims

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.players.PlayerManager
import me.elephant1214.nogrief.utils.ClaimSerializer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

@Serializable(ClaimSerializer::class)
class Claim(
    val claimId: UUID = ClaimManager.newClaimID(),
    ownerIn: UUID,
    val world: World,
    private val _chunks: MutableSet<ClaimChunk> = mutableSetOf(),
    private val _permissions: MutableMap<UUID, EnumSet<ClaimPermission>> = mutableMapOf(
        ownerIn to EnumSet.allOf(ClaimPermission::class.java)
    ),
    modifiedIn: Instant = Instant.now()
) {
    var owner: UUID = ownerIn
        set(value) {
            field = value
            setPermissions(Bukkit.getPlayer(owner)!!, EnumSet.allOf(ClaimPermission::class.java))
        }

    var modified: Instant = modifiedIn
        private set

    constructor(owner: UUID, world: World, chunk: ClaimChunk) : this(
        ownerIn = owner,
        world = world,
    ) {
        this._chunks.add(chunk)
    }

    /**
     * @return Whether [player] has [permission] in this claim.
     */
    private fun hasPermission(player: Player, permission: ClaimPermission): Boolean =
        PlayerManager.isBypassing(player) || this._permissions[player.uniqueId]?.contains(permission) ?: false

    fun canBreak(player: Player): Boolean = hasPermission(player, ClaimPermission.BREAK)
    
    fun canPlace(player: Player): Boolean = hasPermission(player, ClaimPermission.PLACE)
    
    fun canAccessContainers(player: Player): Boolean = hasPermission(player, ClaimPermission.CONTAINERS)
    
    fun hasEntitiesPerm(player: Player): Boolean = hasPermission(player, ClaimPermission.ENTITIES)
    
    fun hasExplosionPerm(player: Player): Boolean = hasPermission(player, ClaimPermission.EXPLOSIONS)
    
    fun hasFirePerm(player: Player): Boolean = hasPermission(player, ClaimPermission.FIRE)
    
    fun canInteract(player: Player): Boolean = hasPermission(player, ClaimPermission.INTERACT)
    
    fun hasTilePerm(player: Player): Boolean = hasPermission(player, ClaimPermission.TILE_ENTITIES)
    
    fun canManageClaim(player: Player): Boolean = hasPermission(player, ClaimPermission.MANAGE)
    
    /**
     * Sets a [ClaimPermission] for a player.
     */
    fun setPermission(player: Player, permission: ClaimPermission) {
        val oldSize = this._permissions.size
        this._permissions.getOrPut(player.uniqueId) { EnumSet.of(permission) }.apply {
            add(permission)
            if (size != oldSize) this@Claim.markModified()
        }
    }

    /**
     * Sets multiple [ClaimPermission]s for a player.
     */
    fun setPermissions(player: Player, permissions: EnumSet<ClaimPermission>) {
        val oldSize = this._permissions.size
        this._permissions.getOrPut(player.uniqueId) { permissions }.apply {
            addAll(permissions)
            if (size != oldSize) this@Claim.markModified()
        }
    }

    /**
     * @return Whether this claim contains the chunk [chunk].
     */
    fun containsChunk(chunk: ClaimChunk): Boolean = chunk in this._chunks

    /**
     * Does the same as [containsChunk], but converts a [Chunk] to a [ClaimChunk] first.
     */
    fun containsChunk(chunk: Chunk): Boolean = this.containsChunk(ClaimChunk(chunk.world, chunk.chunkKey))

    /**
     * Adds a chunk to this claim.
     * @return A claim chunk add result.
     */
    fun addChunk(chunk: ClaimChunk): ClaimChunkAddResult {
        if (chunk.world != this.world) return ClaimChunkAddResult.FAILED_WRONG_WORLD
        if (ClaimManager.getClaim(chunk) == null) {
            this._chunks.add(chunk)
            this.markModified()
            return ClaimChunkAddResult.SUCCESS
        } else {
            return ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED
        }
    }

    /**
     * Removes a chunk from this claim.
     * @return Whether this remove call removed a chunk.
     */
    fun removeChunk(chunk: ClaimChunk): Boolean = this._chunks.remove(chunk).also { if (it) this.markModified() }

    /**
     * Sets [modified] to the current instant.
     */
    private fun markModified() {
        this.modified = Instant.now()
    }

    /**
     * For serial use only.
     */
    internal fun getChunksForSerial(): Set<Long> = this._chunks.map { it.chunk }.toSet()

    /**
     * For serial use only.
     */
    internal fun getPermsForSerial(): Map<UUID, EnumSet<ClaimPermission>> = this._permissions.toMap()
}
