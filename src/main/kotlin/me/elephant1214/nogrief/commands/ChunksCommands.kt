package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.constants.CLAIM
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import me.elephant1214.nogrief.utils.hasClaimChunks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object ChunksCommands {
    @CommandMethod("chunks transfer <player> <chunks>")
    @BetterCmdPerm(CLAIM, permDefault = PermissionDefault.TRUE)
    @CommandDescription("Transfers unused claim chunks to the specified player.")
    fun giveChunks(
        sender: Player,
        @Argument("player") player: Player,
        @Argument("chunks") chunks: Int,
    ) {
        if (!sender.hasClaimChunks()) return
        val senderData = PlayerManager.getPlayer(sender)
        if (senderData.remainingClaimChunks < chunks) {
            sender.sendMessage(
                LocaleManager.get(
                    "chunks.transfer.notEnough",
                    Placeholder.component("chunks", Component.text(chunks)),
                    Placeholder.component("player", player.displayName())
                )
            )
        }

        val playerData = PlayerManager.getPlayer(player)
        senderData.remainingClaimChunks -= chunks
        playerData.remainingClaimChunks += chunks
        sender.sendMessage(
            LocaleManager.get(
                "chunks.transfer.sent",
                Placeholder.component("chunks", Component.text(chunks)),
                Placeholder.component("player", player.displayName())
            )
        )
        player.sendMessage(
            LocaleManager.get(
                "chunks.transfer.received",
                Placeholder.component("chunks", Component.text(chunks)),
                Placeholder.component("player", player.displayName())
            )
        )
    }
}