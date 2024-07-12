package me.elephant1214.nogrief.hooks.pl3xmap

import net.pl3x.map.core.Pl3xMap
import net.pl3x.map.core.event.EventHandler
import net.pl3x.map.core.event.EventListener
import net.pl3x.map.core.event.server.ServerLoadedEvent
import net.pl3x.map.core.event.world.WorldLoadedEvent
import net.pl3x.map.core.event.world.WorldUnloadedEvent

@Suppress("unused")
object Pl3xHookListener : EventListener {
    internal fun init() {
        Pl3xMap.api().eventRegistry.register(this)
    }
    
    @EventHandler
    fun onServerLoaded(event: ServerLoadedEvent) {
        Pl3xMap.api().worldRegistry.forEach(Pl3xMapHook::registerWorld)
    }

    @EventHandler
    fun onWorldLoaded(event: WorldLoadedEvent) {
        Pl3xMapHook.registerWorld(event.world)
    }
    
    @EventHandler
    fun onWorldUnloaded(event: WorldUnloadedEvent) {
        Pl3xMapHook.unloadWorld(event.world)
    }
}
