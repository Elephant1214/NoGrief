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
        if (event.entity is Enemy) return
        val player = when {
            event.damager is Projectile && (event.damager as Projectile).shooter is Player -> (event.damager as Projectile).shooter as Player
            event.damager is Player -> event.damager as Player
            else -> return
        }

        player.let {
            val claim = ClaimManager.getClaim(event.entity.chunk)
            val attackerClaim = ClaimManager.getClaim(player.chunk)
            if (claim == null && attackerClaim == null) {
                if (!NoGrief.cfg.allowPvpOutsideClaims) {
                    event.isCancelled = true
                    player.sendCantDoThisHere()
                }
                return@onEntityDamageEntity
            }
            
            if (claim != null) {
                val hasEntitiesPerm = claim.hasEntitiesPerm(player)
                if (NoGrief.cfg.allowPvpInClaims && event.entity is Player) return@onEntityDamageEntity
                val isEndCrystal = event.entity is EndCrystal
                val hasOtherCrystalPerms = claim.canBreak(player) && claim.hasExplosionPerm(player)
                if (hasEntitiesPerm && isEndCrystal && hasOtherCrystalPerms) return@onEntityDamageEntity
            }

            event.isCancelled = true
            player.sendCantDoThisHere()
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
