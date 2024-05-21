package me.elephant1214.nogrief.bootstrap

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import me.elephant1214.nogrief.NoGrief
import org.bukkit.plugin.java.JavaPlugin

class NoGriefBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {}

    override fun createPlugin(context: PluginProviderContext): JavaPlugin = NoGrief
}