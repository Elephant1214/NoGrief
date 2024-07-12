package me.elephant1214.nogrief.players

import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.constants.ADMIN
import me.elephant1214.nogrief.constants.CLAIM_BYPASS
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

object PlayerManager : Listener {
    private val playersDir: Path =
        NoGrief.dataDir.resolve("players").apply { if (!this@apply.exists()) this@apply.createDirectories() }

    private val _playerData = mutableMapOf<UUID, PlayerData>()

    fun getPlayer(player: OfflinePlayer): PlayerData =
        this._playerData[player.uniqueId] ?: this.loadData(player).let { this._playerData[player.uniqueId]!! }

    fun setBypassMode(player: Player, state: Boolean) {
        if (player.hasPermission(CLAIM_BYPASS)) {
            this.getPlayer(player).inBypassMode = state
        }
    }

    fun inBypassMode(player: OfflinePlayer): Boolean = this.getPlayer(player).inBypassMode

    fun setAdminClaimMode(player: Player, state: Boolean) {
        if (player.hasPermission(ADMIN)) {
            this.getPlayer(player).inAdminClaimMode = state
        }
    }

    fun inAdminClaimMode(player: Player): Boolean = this.getPlayer(player).inAdminClaimMode

    internal fun saveData() {
        this._playerData.forEach { (uuid, playerData) ->
            NoGrief.JSON.encodeToStream(playerData, this.getPlayerDataPath(uuid).outputStream())
        }
    }

    private fun loadData(player: OfflinePlayer) {
        val playerData = loadPlayerData(this.getPlayerDataPath(player.uniqueId))
        this._playerData[player.uniqueId] = playerData
    }

    private fun getPlayerDataPath(uuid: UUID): Path = this.playersDir.resolve("$uuid.json").apply {
        if (!this.exists()) this.createFile()
    }

    private fun loadPlayerData(path: Path): PlayerData {
        return try {
            NoGrief.JSON.decodeFromStream<PlayerData>(path.inputStream())
        } catch (e: Exception) {
            val data = PlayerData()
            NoGrief.JSON.encodeToStream<PlayerData>(data, path.outputStream())
            data
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        loadData(event.player)
    }
}
