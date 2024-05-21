package me.elephant1214.nogrief

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.claims.listeners.BlockListener
import me.elephant1214.nogrief.claims.listeners.EntityListener
import me.elephant1214.nogrief.claims.listeners.ExplosionListener
import me.elephant1214.nogrief.claims.listeners.InteractListener
import me.elephant1214.nogrief.configuration.NoGriefConfig
import me.elephant1214.nogrief.listeners.DataListener
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.io.path.*

object NoGrief : JavaPlugin() {
    internal val JSON = Json {
        encodeDefaults = true
    }
    private val PRETTY_JSON = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    val dataDir: Path = this.dataFolder.toPath().apply { 
        if (!this@apply.exists()) this@apply.createDirectories()
    }
    private val cfgFile = dataDir.resolve("config.json").apply {
        if (!this@apply.exists()) this@apply.createFile()
    }
    var cfg: NoGriefConfig
        private set
    
    init {
        this.cfg = loadCfg()
        this.saveCfg()
    }

    override fun onEnable() {
        ClaimManager.loadClaims()
        this.registerListeners()
    }

    override fun onDisable() {
        this.fullSave()
    }

    private fun loadCfg(): NoGriefConfig = try {
        JSON.decodeFromStream<NoGriefConfig>(this.cfgFile.inputStream())
    } catch (e: Exception) {
        NoGriefConfig()
    }

    fun reloadCfg() {
        this.cfg = loadCfg()
    }

    fun saveCfg() {
        PRETTY_JSON.encodeToStream(this.cfg, this.cfgFile.outputStream())
    }

    private fun registerListeners() {
        val listeners = listOf(BlockListener, EntityListener, ExplosionListener, InteractListener, DataListener)
        listeners.forEach { this.server.pluginManager.registerEvents(it, this) }
    }

    internal fun fullSave() {
        PlayerManager.saveData()
        ClaimManager.saveClaims()
    }
}
