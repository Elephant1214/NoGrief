package me.elephant1214.nogrief

import me.elephant1214.nogrief.claims.ClaimManager
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

object NoGrief : JavaPlugin() {
    val dataDir: Path = this.dataFolder.toPath()
    
    override fun onEnable() {
        saveDefaultConfig()
        ClaimManager.loadClaims()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
