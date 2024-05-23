package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimChunk
import me.elephant1214.nogrief.claims.ClaimChunkAddResult
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.constants.*
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import java.util.*

@Suppress("unused")
object ClaimCommands {
    @CommandMethod("newclaim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Creates a claim in the chunk you're standing in.")
    fun createClaim(
        sender: Player,
    ) {
        if (!hasClaimChunks(sender)) return

        when (Claim.createClaim(sender, ClaimChunk(sender.chunk))) {
            ClaimChunkAddResult.SUCCESS -> sender.sendMessage(CREATED_CLAIM)
            ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED -> sender.sendMessage(ALREADY_CLAIMED)
            ClaimChunkAddResult.FAILED_WRONG_WORLD -> error("Impossible state")
        }
    }

    @CommandMethod("delclaim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Deletes the claim at the current chunk you're standing in.")
    fun deleteClaim(
        sender: Player,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendMessage(NOT_IN_CLAIM)
            return
        }
        
        if (!claim.isOwner(sender)) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        
        ClaimManager.deleteClaim(claim)
        sender.sendMessage(DELETE_CLAIM)
    }

    @CommandMethod("claim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Looks for a connected surrounding claim you manage, then adds the chunk to the claim.")
    fun claimChunk(
        sender: Player,
    ) {
        val currentClaim = ClaimManager.getClaim(sender.chunk)
        if (currentClaim != null) {
            sender.sendMessage(ALREADY_CLAIMED)
            return
        }
        
        val chunk = findClaim(sender)
        if (chunk == null) {
            sender.sendMessage(NO_CONNECTING_CLAIM)
            return
        }
        
        val claim = ClaimManager.getClaim(chunk)!!
        if (sender.uniqueId != claim.owner) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        claim.addChunk(ClaimChunk(sender.chunk))
        sender.sendChunkClaimed(sender.chunk)
    }

    @CommandMethod("unclaim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Unclaims the chunk you're standing in.")
    fun unclaimChunk(
        sender: Player,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendMessage(NOT_IN_CLAIM)
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendMessage(NO_PERMISSION)
            return
        }

        claim.removeChunk(ClaimChunk(sender.chunk))
        sender.sendMessage(REMOVE_CHUNK)
        
        if (claim.chunkCount() <= 0) {
            ClaimManager.deleteClaim(claim)
            sender.sendMessage(DELETE_CLAIM_NO_CHUNKS)
        }
    }

    // @CommandMethod("claimall")
    // @CommandDescription("Claims all available chunks you walk into until you stop the command or have no claim chunks available.")
    // @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    // fun claimAll(
    //     sender: Player
    // ) {
    //     if (!hasClaimChunks(sender)) return
    // }

    @CommandMethod("claimchunks")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Shows your current claim chunk count.")
    fun claimChunks(
        sender: Player,
    ) {
        sender.sendClaimChunkCount()
    }

    @CommandMethod("claim permission <target> <permission> <state>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Changes a permission for the specified player in your current claim. Giving a player manage gives them ALL permissions.")
    fun setPermission(
        sender: Player,
        @Argument("target") target: Player,
        @Argument("permission") permission: ClaimPermission,
        @Argument("state") state: Boolean,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendMessage(NOT_IN_CLAIM)
            return
        }
        
        if (!claim.canManageClaim(sender)) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        
        if (claim.canManageClaim(target) && !claim.isOwner(sender)) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        
        if (permission == ClaimPermission.MANAGE) {
            if (state) {
                claim.setPermissions(target, EnumSet.allOf(ClaimPermission::class.java), true)
            } else {
                claim.setPermission(target, ClaimPermission.MANAGE, false)
            }
        } else {
            claim.setPermission(target, permission, state)
        }
        
        sender.sendPermissionsUpdated(permission, target)
    }

    @CommandMethod("claim transfer <target>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Transfers ownership of the current claim to the target.")
    fun transfer(
        sender: Player,
        @Argument("target") target: Player,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendMessage(NOT_IN_CLAIM)
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        
        claim.owner = target.uniqueId
        sender.sendTransferredOwnership(claim.name, target)
        target.sendNewOwner(claim.name)
    }

    @CommandMethod("claim rename <name>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Renames the current claim.")
    fun setOwner(
        sender: Player,
        @Argument("name") name: String,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendMessage(NOT_IN_CLAIM)
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendMessage(NO_PERMISSION)
            return
        }
        val oldName = claim.name
        sender.sendRenamed(oldName, name)
        claim.name = name
    }

    private fun hasClaimChunks(player: Player): Boolean {
        if (!PlayerManager.getPlayer(player).hasClaimChunks()) {
            player.sendMessage(NOT_ENOUGH_CLAIM_CHUNKS)
            return false
        }
        return true
    }

    /**
     * Finds a claim owned by [player] that is N, E, S, or W of the player's current chunk.
     */
    private fun findClaim(player: Player): ClaimChunk? {
        val currentChunk = player.chunk
        val surrounding = mutableListOf(ClaimChunk(currentChunk))
        
        val directions = listOf(
            Pair(0, -1),
            Pair(0, 1),
            Pair(-1, 0),
            Pair(1, 0),
        )
        
        for ((x, z) in directions) {
            val chunk = ClaimChunk(currentChunk.world, currentChunk.x + x, currentChunk.z + z)
            surrounding.add(chunk)
        }

        surrounding.forEach {
            val claim = ClaimManager.getClaim(it)
            if (claim != null && claim.canManageClaim(player)) {
                return@findClaim it
            }
        }

        return null
    }
}