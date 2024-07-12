package me.elephant1214.nogrief.hooks.pl3xmap

import net.pl3x.map.core.markers.layer.WorldLayer
import net.pl3x.map.core.markers.marker.Marker
import net.pl3x.map.core.world.World

class NoGriefLayer(
    world: World
) : WorldLayer(
    KEY,
    world,
    { "No Grief" }
) {
    init {
        setShowControls(true)
        isDefaultHidden = false
        updateInterval = 30
        priority = 10
        zIndex = 10
    }

    override fun getMarkers(): MutableList<Marker<*>> = Pl3xMapHook.getMarkers(this.world)

    companion object {
        const val KEY: String = "nogrief"
    }
}
