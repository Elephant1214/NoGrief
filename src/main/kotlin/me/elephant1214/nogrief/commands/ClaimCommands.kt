package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimChunk
import me.elephant1214.nogrief.claims.ClaimChunkAddResult
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.CLAIM
import me.elephant1214.nogrief.constants.sendNoPermission
import me.elephant1214.nogrief.constants.sendNotInAClaim
import me.elephant1214.nogrief.constants.toMsgComp
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.utils.hasClaimChunks
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object ClaimCommands {
    @CommandMethod("claim")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Claims the chunk you're standing in. Also creates a new claim if there isn't one in a surrounding chunk.")
    fun claim(
        sender: Player,
    ) {
        if (!sender.hasClaimChunks()) return

        val chunk = findClaim(sender)
        if (chunk == null) {
            when (Claim.createClaim(sender, ClaimChunk(sender.chunk))) {
                ClaimChunkAddResult.SUCCESS -> sender.sendMessage(
                    LocaleManager.get(
                        "claim.created", Placeholder.component("chunk", sender.chunk.toMsgComp())
                    )
                )

                ClaimChunkAddResult.FAILED_CHUNK_ALREADY_CLAIMED -> sender.sendMessage(LocaleManager.get("chunk.alreadyClaimed"))
                ClaimChunkAddResult.FAILED_WRONG_WORLD -> error("Impossible state")
            }
        } else {
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
