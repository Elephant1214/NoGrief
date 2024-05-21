package me.elephant1214.nogrief.listeners

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldSaveEvent

object DataListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        PlayerManager.loadData(event.player)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onWorldSave(event: WorldSaveEvent) {
        NoGrief.fullSave()
    }
}