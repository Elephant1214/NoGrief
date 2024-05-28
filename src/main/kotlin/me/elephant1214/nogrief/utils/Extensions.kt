package me.elephant1214.nogrief.utils

import me.elephant1214.nogrief.constants.sendCantDoThisHere
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

fun PlayerInteractEvent.cancelInteractEvent() {
    this.isCancelled = true
    this.player.sendCantDoThisHere()
}

fun Player.hasClaimChunks(): Boolean {
    if (!PlayerManager.getPlayer(this).hasClaimChunks()) {
        this.sendMessage(LocaleManager.get("player.notEnoughClaimChunks"))
        return false
    }
    return true
}
