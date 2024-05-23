package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.constants.CLAIM_BYPASS
import me.elephant1214.nogrief.constants.sendClaimBypassState
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object AdminCommands {
    @CommandMethod("nogrief bypass")
    @BetterCmdPerm(CLAIM_BYPASS, permDefault = PermissionDefault.OP)
    @CommandDescription("Toggles claim bypassing.")
    fun createClaim(
        sender: Player,
    ) {
        val newState = !PlayerManager.isBypassing(sender)
        PlayerManager.setBypass(sender, newState)
        sender.sendClaimBypassState(newState)
    }
}