package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.utils.sendNoPermission
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

object ExplosionListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val claim = ClaimManager.getClaim(event.location.chunk) ?: return
        if (event.entity is Creeper) {
            val target = (event.entity as Creeper).target ?: return
            if (target !is Player || claim.hasExplosionPerm(target)) return
            target.sendNoPermission()
        } else if (event.entity is TNTPrimed) {
            var source: Entity = (event.entity as TNTPrimed).source ?: return
            while (source is TNTPrimed && source.source != null) {
                source = source.source!!
            }
            if (source !is Player || claim.hasExplosionPerm(source)) return
            source.sendNoPermission()
        }
        event.isCancelled = true
    }
}