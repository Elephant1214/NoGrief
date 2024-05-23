package me.elephant1214.nogrief.players

import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.configuration.PlayerManagerConfig
import org.bukkit.OfflinePlayer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

object PlayerManager {
    private val playersDir: Path =
        NoGrief.dataDir.resolve("players").apply { if (!this@apply.exists()) this@apply.createDirectories() }
    private val playerManagerConfig: Path =
        NoGrief.dataDir.resolve("playerManager.json").apply { if (!this@apply.exists()) this@apply.createFile() }

    private val _playerData = mutableMapOf<UUID, PlayerData>()
    private val _config: PlayerManagerConfig = loadConfig()

    fun getPlayer(player: OfflinePlayer): PlayerData =
        this._playerData[player.uniqueId] ?: this.loadData(player).let { this._playerData[player.uniqueId]!! }

    fun setBypass(player: OfflinePlayer, state: Boolean) = if (state) {
        this._config.bypassPlayers.add(player.uniqueId)
    } else {
        this._config.bypassPlayers.remove(player.uniqueId)
    }

    fun isBypassing(player: OfflinePlayer): Boolean = player.uniqueId in this._config.bypassPlayers

    internal fun saveData() {
        this._playerData.forEach { (uuid, playerData) ->
            NoGrief.JSON.encodeToStream(playerData, this.getPlayerDataPath(uuid).outputStream())
        }
        NoGrief.JSON.encodeToStream(this._config, this.playerManagerConfig.outputStream())
    }

    internal fun loadData(player: OfflinePlayer) {
        val playerData = loadPlayerData(this.getPlayerDataPath(player.uniqueId))
        this._playerData[player.uniqueId] = playerData
    }

    private fun getPlayerDataPath(uuid: UUID): Path = this.playersDir.resolve("$uuid.json").apply {
        if (!this.exists()) this.createFile()
    }

    private fun loadConfig(): PlayerManagerConfig {
        return try {
            NoGrief.JSON.decodeFromStream<PlayerManagerConfig>(playerManagerConfig.inputStream())
        } catch (e: Exception) {
            val cfg = PlayerManagerConfig()
            NoGrief.JSON.encodeToStream<PlayerManagerConfig>(cfg, this.playerManagerConfig.outputStream())
            cfg
        }
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
}