package me.elephant1214.nogrief

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.ccfutils.CCFUtils
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.claims.listeners.BlockListener
import me.elephant1214.nogrief.claims.listeners.EntityListener
import me.elephant1214.nogrief.claims.listeners.ExplosionListener
import me.elephant1214.nogrief.claims.listeners.InteractListener
import me.elephant1214.nogrief.commands.AdminCommands
import me.elephant1214.nogrief.commands.ClaimCommands
import me.elephant1214.nogrief.configuration.NoGriefConfig
import me.elephant1214.nogrief.listeners.DataListener
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.command.CommandSender
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

    private lateinit var commandManager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    var cfg: NoGriefConfig
        private set
    
    init {
        this.cfg = loadCfg()
        this.saveCfg()
    }

    override fun onEnable() {
        this.setupCommandManager()
        this.registerCommands()
        
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

    private fun setupCommandManager() {
        val mapper: java.util.function.Function<CommandSender, CommandSender> = java.util.function.Function.identity()
        commandManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            mapper,
            mapper
        )

        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier()
        }

        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions()
        }

        val cmdMetaFun = java.util.function.Function<ParserParameters, CommandMeta> { p ->
            CommandMeta.simple()
                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                .build()
        }

        annotationParser = AnnotationParser(
            commandManager,
            CommandSender::class.java,
            cmdMetaFun
        )
    }

    private fun registerCommands() {
        if (this::annotationParser.isInitialized) {
            // Annotations
            CCFUtils.registerAnnotations(annotationParser)

            // Annotation commands
            annotationParser.parse(ClaimCommands)
            annotationParser.parse(AdminCommands)
        }
    }

    internal fun fullSave() {
        PlayerManager.saveData()
        ClaimManager.saveClaims()
    }
}
