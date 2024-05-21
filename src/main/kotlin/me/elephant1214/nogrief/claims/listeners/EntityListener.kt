package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.utils.sendNoPermission
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
    fun onEntityDamageEndCrystal(event: EntityDamageByEntityEvent) {
        val player = when {
            event.damager is Player && (event.damager is Projectile) -> (event.damager as Projectile).shooter as Player
            event.damager is Player -> event.damager as Player
            else -> return
        }

        player.let {
            val claim = ClaimManager.getClaim(event.entity.chunk) ?: return@onEntityDamageEndCrystal
            if (event.entity is Enemy) return@onEntityDamageEndCrystal

            val hasEntitiesPerm = claim.hasEntitiesPerm(player)
            val isEndCrystal = event.entity is EndCrystal
            val hasOtherCrystalPerms = claim.canBreak(player) && claim.hasExplosionPerm(player)

            if (!hasEntitiesPerm || (isEndCrystal && hasOtherCrystalPerms)) {
                event.isCancelled = true
                player.sendNoPermission()
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val claim = ClaimManager.getClaim(event.rightClicked.chunk) ?: return
        if (claim.hasEntitiesPerm(event.player)) return
        event.isCancelled = true
        event.player.sendNoPermission()
    }
}
