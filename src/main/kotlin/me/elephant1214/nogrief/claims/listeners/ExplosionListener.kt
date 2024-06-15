package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.sendCantDoThisHere
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

object ExplosionListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onEntityExplode(event: EntityExplodeEvent) {
        var causingEntity: Entity? = null

        when (event.entity) {
            is Creeper -> {
                causingEntity = (event.entity as Creeper).target ?: return
            }

            is TNTPrimed -> {
                causingEntity = (event.entity as TNTPrimed).source ?: return
                while (causingEntity is TNTPrimed && causingEntity.source != null) {
                    causingEntity = causingEntity.source!!
                }
                if (causingEntity is TNTPrimed && causingEntity.source == null) {
                    event.isCancelled = true
                    return
                }
            }

            is Fireball -> {
                val shooter = (event.entity as Fireball).shooter
                if (shooter != null && shooter is Mob) {
                    causingEntity = ((event.entity as Fireball).shooter as Mob).target
                }
            }
        }

        if (causingEntity is Player) {
            event.blockList().forEach { block ->
                val claim = ClaimManager.getClaim(block.chunk)
                if (claim != null && !claim.hasExplosionPerm(causingEntity)) {
                    event.isCancelled = true
                    causingEntity.sendCantDoThisHere()
                    return@onEntityExplode
                }
            }
        }
    }
}
