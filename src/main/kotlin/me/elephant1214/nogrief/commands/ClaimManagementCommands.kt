package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.specifier.Greedy
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.ClaimColor
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.CLAIM
import me.elephant1214.nogrief.constants.sendNoPermission
import me.elephant1214.nogrief.constants.sendNotInAClaim
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.menus.PermissionsMenu
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object ClaimManagementCommands {
    @CommandMethod("claim permissions <target>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Allows you to change the specified player's permissions in your current claim.")
    fun permissionManagement(
        sender: Player,
        @Argument("target") target: OfflinePlayer,
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
        
        PermissionsMenu.permissionsMenu(sender, claim, target)
    }

    @CommandMethod("claim color <color>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Changes a map color of a claim.")
    fun setColor(
        sender: Player,
        @Argument("color") color: ClaimColor,
    ) {
        val claim = ClaimManager.getClaim(sender.chunk)
        if (claim == null) {
            sender.sendNotInAClaim()
            return
        }
        if (!claim.canManageClaim(sender)) {
            sender.sendNoPermission()
            return
        }

        sender.sendMessage(
            LocaleManager.get(
                "claim.color.updated",
                Placeholder.component("color", Component.text(color.toString().lowercase()))
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

    @CommandMethod("claim rename <new>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Renames the current claim.")
    fun rename(
        sender: Player,
        @Greedy @Argument("new") new: String,
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

        val name = NoGrief.MINI_MESSAGE.deserialize(new)
        val oldName = claim.name
        sender.sendMessage(
            LocaleManager.get(
                "claim.renamed", Placeholder.component("old", oldName), Placeholder.component("new", name)
            )
        )
        claim.name = name
    }
}