package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.constants.CLAIM
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object ClaimInfoCommands {
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
}