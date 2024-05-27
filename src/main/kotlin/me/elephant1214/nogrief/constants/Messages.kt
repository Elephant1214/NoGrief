package me.elephant1214.nogrief.constants

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.locale.LocaleManager
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.entity.Player

fun Chunk.toMsgComp(): Component = Component.text("${this.x}, ${this.z}")

fun Player.sendCantDoThisHere() {
    this.sendActionBar(LocaleManager.get("cantDoThisHere"))
}

fun Player.sendPistonMessage() {
    if (!NoGrief.cfg.allowPistonsOutsideClaims) {
        this.sendActionBar(LocaleManager.get("wild.noPistons"))
    }
}

fun Player.sendNotInAClaim() {
    this.sendMessage(LocaleManager.get("claim.noClaim"))
}

fun Player.sendNoPermission() {
    this.sendMessage(LocaleManager.get("claim.noPermission"))
}
