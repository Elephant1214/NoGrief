package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimChunk
import me.elephant1214.nogrief.claims.ClaimChunkAddResult
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.constants.CLAIM
import me.elephant1214.nogrief.constants.sendNoPermission
import me.elephant1214.nogrief.constants.sendNotInAClaim
import me.elephant1214.nogrief.constants.toMsgComp
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
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
            ClaimChunkAddResult.SUCCESS -> sender.sendMessage(
                LocaleManager.get(
                    "claim.created", Placeholder.component("chunk", sender.chunk.toMsgComp())
                )
            )

            ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED -> sender.sendMessage(LocaleManager.get("chunk.alreadyClaimed"))
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
            sender.sendNotInAClaim()
            return
        }

        if (!claim.isOwner(sender)) {
            sender.sendNoPermission()
            return
        }

        ClaimManager.deleteClaim(claim)
        sender.sendMessage(LocaleManager.get("claim.deleted", Placeholder.component("claim", claim.name)))
    }

    @CommandMethod("claim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Looks for a connected surrounding claim you manage, then adds the chunk to the claim.")
    fun claimChunk(
        sender: Player,
    ) {
        val currentClaim = ClaimManager.getClaim(sender.chunk)
        if (currentClaim != null) {
            sender.sendMessage(LocaleManager.get("chunk.alreadyClaimed"))
            return
        }

        val chunk = findClaim(sender)
        if (chunk == null) {
            sender.sendMessage(LocaleManager.get("chunk.noConnectingClaim"))
            return
        }

        val claim = ClaimManager.getClaim(chunk)!!
        if (sender.uniqueId != claim.owner) {
            sender.sendNoPermission()
            return
        }
        claim.addChunk(ClaimChunk(sender.chunk))
        sender.sendMessage(
            LocaleManager.get(
                "chunk.claimed", Placeholder.component("chunk", sender.chunk.toMsgComp())
            )
        )
    }

    @CommandMethod("unclaim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Unclaims the chunk you're standing in.")
    fun unclaimChunk(
        sender: Player,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendNotInAClaim()
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendNoPermission()
            return
        }

        claim.removeChunk(ClaimChunk(sender.chunk))
        sender.sendMessage(
            LocaleManager.get(
                "chunk.removed", Placeholder.component("chunk", sender.chunk.toMsgComp())
            )
        )

        if (claim.chunkCount() <= 0) {
            ClaimManager.deleteClaim(claim)
            sender.sendMessage(LocaleManager.get("claim.deleted.noChunks"))
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
        sender.sendMessage(
            LocaleManager.get(
                "player.remainingChunks", Placeholder.component(
                    "remaining", Component.text(PlayerManager.getPlayer(sender).remainingClaimChunks.toString())
                ), Placeholder.component(
                    "total", Component.text(PlayerManager.getPlayer(sender).totalClaimChunks.toString())
                )
            )
        )
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
            sender.sendNotInAClaim()
            return
        }

        if (!claim.canManageClaim(sender) || (claim.canManageClaim(target) && !claim.isOwner(sender))) {
            sender.sendNoPermission()
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

        sender.sendMessage(
            LocaleManager.get(
                "claim.permissions.updated",
                Placeholder.component("permission", Component.text(permission.toString().replace('_', ' '))),
                Placeholder.component("player", target.displayName())
            )
        )
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
            sender.sendMessage(LocaleManager.get("claim.noClaim"))
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendNoPermission()
            return
        }

        if (PlayerManager.getPlayer(Bukkit.getOfflinePlayer(target.uniqueId)).remainingClaimChunks < claim.chunkCount()) {
            sender.sendMessage(
                LocaleManager.get(
                    "claim.transfer.notEnoughChunks", Placeholder.component("player", target.displayName())
                )
            )
            return
        }

        PlayerManager.getPlayer(claim.getPlayerOwner()).remainingClaimChunks += claim.chunkCount()
        claim.owner = target.uniqueId
        PlayerManager.getPlayer(Bukkit.getOfflinePlayer(target.uniqueId)).remainingClaimChunks -= claim.chunkCount()
        sender.sendMessage(
            LocaleManager.get(
                "claim.transfer.success",
                Placeholder.component("claim", claim.name),
                Placeholder.component("player", target.displayName())
            )
        )
        target.sendMessage(
            LocaleManager.get(
                "claim.transfer.newOwner", Placeholder.component("claim", target.displayName())
            )
        )
    }

    @CommandMethod("claim rename <name>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Renames the current claim.")
    fun rename(
        sender: Player,
        @Argument("name") nameIn: String,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendNotInAClaim()
            return
        }

        if (sender.uniqueId != claim.owner) {
            sender.sendNoPermission()
            return
        }

        val name = NoGrief.MINI_MESSAGE.deserialize(nameIn)
        val oldName = claim.name
        sender.sendMessage(
            LocaleManager.get(
                "claim.renamed", Placeholder.component("old", oldName), Placeholder.component("new", name)
            )
        )
        claim.name = name
    }

    private fun hasClaimChunks(player: Player): Boolean {
        if (!PlayerManager.getPlayer(player).hasClaimChunks()) {
            player.sendMessage(LocaleManager.get("player.notEnoughClaimChunks"))
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
