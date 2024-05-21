package me.elephant1214.nogrief.utils

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.constants.NO_PERMISSION
import me.elephant1214.nogrief.constants.NO_PISTONS_OUTSIDE_CLAIMS
import net.kyori.adventure.title.TitlePart
import org.bukkit.entity.Player

fun Player.sendNoPermission() {
    this.sendTitlePart(TitlePart.SUBTITLE, NO_PERMISSION)
}

fun Player.sendPistonMessage() {
    if (!NoGrief.cfg.allowPistonsOutsideOfClaims) {
        this.sendTitlePart(TitlePart.SUBTITLE, NO_PISTONS_OUTSIDE_CLAIMS)
    }
}
