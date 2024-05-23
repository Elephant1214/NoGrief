package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.sendNoPermission
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
        lateinit var causingEntity: Entity
        
        if (event.entity is Creeper) {
            val target = (event.entity as Creeper).target ?: return
            if (target !is Player) return
        } else if (event.entity is TNTPrimed) {
            causingEntity = (event.entity as TNTPrimed).source ?: return
            while (causingEntity is TNTPrimed && causingEntity.source != null) {
                causingEntity = causingEntity.source!!
            }
            if (causingEntity !is Player) return
        }
        
        if (causingEntity is Player) {
            event.blockList().forEach { block ->
                val claim = ClaimManager.getClaim(block.chunk)
                if (claim != null && !claim.hasExplosionPerm(causingEntity)) {
                    event.isCancelled = true
                    causingEntity.sendNoPermission()
                    return@onEntityExplode
                }
            }
        }
    }
}