package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.sendCantDoThisHere
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import org.bukkit.entity.Enemy
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

object EntityListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onEntityDamageEntity(event: EntityDamageByEntityEvent) {
        val player = when {
            event.damager is Projectile && (event.damager as Projectile).shooter is Player -> (event.damager as Projectile).shooter as Player
            event.damager is Player -> event.damager as Player
            else -> return
        }

        player.let {
            val claim = ClaimManager.getClaim(event.entity.chunk) ?: return
            if (event.entity is Enemy) return@onEntityDamageEntity
            if (event.entity is Player && !NoGrief.cfg.allowPvPInClaims) {
                event.isCancelled = true
                player.sendCantDoThisHere()
                return
            } else if (NoGrief.cfg.allowPvPInClaims) {
                return
            }

            val hasEntitiesPerm = claim.hasEntitiesPerm(player)
            val isEndCrystal = event.entity is EndCrystal
            val hasOtherCrystalPerms = claim.canBreak(player) && claim.hasExplosionPerm(player)

            if (!hasEntitiesPerm || (isEndCrystal && hasOtherCrystalPerms)) {
                event.isCancelled = true
                player.sendCantDoThisHere()
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val claim = ClaimManager.getClaim(event.rightClicked.chunk) ?: return
        if (claim.hasEntitiesPerm(event.player)) return
        event.isCancelled = true
        event.player.sendCantDoThisHere()
    }
}
