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
import me.elephant1214.inventorymenus.InventoryMenus
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.claims.listeners.BlockListener
import me.elephant1214.nogrief.claims.listeners.EntityListener
import me.elephant1214.nogrief.claims.listeners.ExplosionListener
import me.elephant1214.nogrief.claims.listeners.InteractListener
import me.elephant1214.nogrief.commands.*
import me.elephant1214.nogrief.configuration.NoGriefConfig
import me.elephant1214.nogrief.hooks.pl3xmap.Pl3xHookListener
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.io.path.*

object NoGrief : JavaPlugin(), Listener {
    internal val JSON = Json {
        encodeDefaults = true
    }
    internal val PRETTY_JSON = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    internal val MINI_MESSAGE = MiniMessage.miniMessage()

    val dataDir: Path = this.dataFolder.toPath().apply {
        if (!this@apply.exists()) this@apply.createDirectories()
    }
    private val cfgFile = dataDir.resolve("config.json")

    private lateinit var commandManager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    var cfg: NoGriefConfig = NoGriefConfig()
        private set

    private fun loadCfg(): NoGriefConfig = try {
        JSON.decodeFromStream<NoGriefConfig>(this.cfgFile.inputStream())
    } catch (e: Exception) {
        NoGriefConfig()
    }

    fun reloadCfg() {
        if (!this.cfgFile.exists()) {
            this.cfgFile.createFile()
            this.saveCfg()
        }
        this.cfg = loadCfg()
    }

    fun saveCfg() {
        PRETTY_JSON.encodeToStream(this.cfg, this.cfgFile.outputStream())
    }

    override fun onEnable() {
        this.reloadCfg()
        LocaleManager.loadMessages()
        this.setupCommandManager()
        this.registerCommands()

        ClaimManager.loadClaims()
        this.registerListeners()
        InventoryMenus.init(this)

        if (Bukkit.getPluginManager().isPluginEnabled("Pl3xMap")) {
            Pl3xHookListener.init()
            this.logger.info("Pl3xMap found, enabled claim display hook")
        }
    }

    override fun onDisable() {
        this.fullSave()
    }

    private fun registerListeners() {
        val listeners: List<Listener> =
            listOf(this, BlockListener, EntityListener, ExplosionListener, InteractListener, PlayerManager)
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
            annotationParser.parse(AdminCommands)
            annotationParser.parse(ChunksCommands)
            annotationParser.parse(ClaimCommands)
            annotationParser.parse(ClaimInfoCommands)
            annotationParser.parse(ClaimManagementCommands)
        }
    }

    private fun fullSave() {
        PlayerManager.saveData()
        ClaimManager.saveClaims()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onWorldSave(event: WorldSaveEvent) {
        fullSave()
    }
}
