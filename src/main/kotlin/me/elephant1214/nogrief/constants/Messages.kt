package me.elephant1214.nogrief.constants

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.locale.LocaleManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Chunk
import org.bukkit.entity.Player

val CANT_DO_THIS_HERE = Component.text("You don't have permission to do this here!", NamedTextColor.RED)
val NO_PISTONS_OUTSIDE_CLAIMS = Component.text("Pistons cannot move outside of claims.", NamedTextColor.YELLOW)

fun Chunk.toMsgComp(): Component = Component.text("${this.x}, ${this.z}")

fun Player.sendCantDoThisHere() {
    this.sendActionBar(CANT_DO_THIS_HERE)
}

fun Player.sendPistonMessage() {
    if (!NoGrief.cfg.allowPistonsOutsideOfClaims) {
        this.sendActionBar(NO_PISTONS_OUTSIDE_CLAIMS)
    }
}

fun Player.sendNotInAClaim() {
    this.sendMessage(LocaleManager.get("claim.noClaim"))
}

fun Player.sendNoPermission() {
    this.sendMessage(LocaleManager.get("claim.noPermission"))
}
