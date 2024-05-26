package me.elephant1214.nogrief.claims

import kotlinx.serialization.Serializable
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.players.PlayerManager
import me.elephant1214.nogrief.utils.ClaimSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

@Serializable(ClaimSerializer::class)
class Claim(
    val claimId: UUID = ClaimManager.newClaimID(),
    var name: Component,
    ownerIn: UUID,
    val world: World,
    private val _chunks: MutableSet<ClaimChunk> = mutableSetOf(),
    private val _permissions: MutableMap<UUID, EnumSet<ClaimPermission>> = mutableMapOf(),
    modifiedIn: Instant = Instant.now()
) {
    var owner: UUID = ownerIn
        set(value) {
            setPermissions(Bukkit.getPlayer(this.owner)!!, EnumSet.allOf(ClaimPermission::class.java), true)
            field = value
        }

    var modified: Instant = modifiedIn
        private set

    private constructor(name: Component, owner: UUID, chunk: ClaimChunk) : this(
        name = name,
        ownerIn = owner,
        world = chunk.world,
    )

    /**
     * Will be null if the player is not in the game.
     */
    fun getPlayerOwner(): OfflinePlayer = Bukkit.getOfflinePlayer(this.owner)

    fun chunkCount(): Int = this._chunks.size

    fun isOwner(player: Player): Boolean = this.owner == player.uniqueId

    /**
     * @return Whether [player] has [permission] in this claim.
     */
    private fun hasPermission(player: Player, permission: ClaimPermission): Boolean =
        this.owner == player.uniqueId || PlayerManager.inBypassClaimMode(player) || this._permissions[player.uniqueId]?.contains(
            permission
        ) ?: false

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
    fun setPermission(player: Player, permission: ClaimPermission, state: Boolean) {
        if (!this._permissions.containsKey(player.uniqueId)) {
            this._permissions[player.uniqueId] = EnumSet.noneOf(ClaimPermission::class.java)
        }

        val set = this._permissions[player.uniqueId]!!
        if (state) {
            set.add(permission)
        } else {
            set.remove(permission)
        }
        if (set.isEmpty()) {
            this._permissions.remove(player.uniqueId)
        }
        
        this@Claim.markModified()
    }

    /**
     * Sets multiple [ClaimPermission]s for a player.
     */
    fun setPermissions(player: Player, permissions: EnumSet<ClaimPermission>, state: Boolean) {
        if (!this._permissions.containsKey(player.uniqueId)) {
            this._permissions[player.uniqueId] = EnumSet.noneOf(ClaimPermission::class.java)
        }

        val set = this._permissions[player.uniqueId]!!
        if (state) {
            set.addAll(permissions)
        } else {
            set.removeAll(permissions)
        }
        if (set.isEmpty()) {
            this._permissions.remove(player.uniqueId)
        }
        
        this@Claim.markModified()
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
            PlayerManager.getPlayer(this.getPlayerOwner()).remainingClaimChunks -= 1
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
    fun removeChunk(chunk: ClaimChunk) {
        val removed = this._chunks.remove(chunk)
        if (removed) {
            PlayerManager.getPlayer(this.getPlayerOwner()).remainingClaimChunks += 1
            this.markModified()
        }
    }

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

    companion object {
        fun createClaim(owner: Player, chunk: ClaimChunk): ClaimChunkAddResult {
            val claim = Claim(
                owner.displayName().append(Component.text("'s Claim", NamedTextColor.AQUA)),
                owner.uniqueId,
                chunk
            )
            val result = claim.addChunk(chunk)
            if (result == ClaimChunkAddResult.SUCCESS) {
                ClaimManager.addClaim(claim)
            }
            return result
        }
    }
}
