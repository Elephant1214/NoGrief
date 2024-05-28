package me.elephant1214.nogrief.hooks.squaremap.hook

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.hooks.squaremap.task.SquaremapTask
import xyz.jpenilla.squaremap.api.Key.key
import xyz.jpenilla.squaremap.api.SimpleLayerProvider
import xyz.jpenilla.squaremap.api.SquaremapProvider
import xyz.jpenilla.squaremap.api.WorldIdentifier

object SquaremapHook {
    private val NO_GRIEF_LAYER_KEY = key("nogrief")
    private val _tasks = mutableMapOf<WorldIdentifier, SquaremapTask>()
    
    init {
        SquaremapProvider.get().mapWorlds().forEach { world ->
            val provider = SimpleLayerProvider.builder("NoGrief").showControls(true).defaultHidden(false).build()
            
            world.layerRegistry().register(NO_GRIEF_LAYER_KEY, provider)
            
            val task = SquaremapTask(world, provider)
            task.runTaskTimerAsynchronously(NoGrief, 20L, 20L * 300)

            _tasks[world.identifier()] = task
        }
    }
    
    fun disable() {
        _tasks.values.forEach(SquaremapTask::disable)
        _tasks.clear()
    }
}