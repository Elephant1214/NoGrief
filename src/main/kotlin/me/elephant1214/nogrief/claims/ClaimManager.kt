package me.elephant1214.nogrief.claims

import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.players.PlayerManager
import org.bukkit.Chunk
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

object ClaimManager {
    private val claimsDir: Path =
        NoGrief.dataDir.resolve("claims").apply { if (!this@apply.exists()) this@apply.createDirectories() }

    private val _claims = mutableSetOf<Claim>()
    val ADMIN_CLAIM_OWNER: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    fun newClaimID(): UUID = UUID.randomUUID()

    fun getClaim(chunk: ClaimChunk): Claim? = this._claims.find { it.containsChunk(chunk) }

    fun getClaim(chunk: Chunk): Claim? = this.getClaim(ClaimChunk(chunk.world, chunk.chunkKey))

    fun addClaim(claim: Claim) = this._claims.add(claim)

    fun deleteClaim(claim: Claim) {
        this._claims.remove(claim)
        PlayerManager.getPlayer(claim.getPlayerOwner()).remainingClaimChunks += claim.chunkCount()
        this.claimsDir.resolve("${claim.claimId}.json").deleteIfExists()
    }

    internal fun saveClaims() {
        try {
            this._claims.forEach { claim ->
                val file = this.getClaimPath(claim.claimId)
                NoGrief.JSON.encodeToStream(claim, file.outputStream())
            }
        } catch (e: Exception) {
            NoGrief.logger.severe("Failed to save claim: ${e.stackTrace}")
        }
    }

    private fun getClaimPath(uuid: UUID): Path = this.claimsDir.resolve("$uuid.json").apply {
        if (!this.exists()) this.createFile()
    }

    internal fun loadClaims() {
        try {
            this.claimsDir.listDirectoryEntries()
                .filter { it.extension == "json" }
                .map { NoGrief.JSON.decodeFromStream<Claim>(it.inputStream()) }
                .forEach { this.addClaim(it) }
        } catch (e: Exception) {
            NoGrief.logger.severe("Failed to load claims:")
            e.printStackTrace()
        }
    }
}
