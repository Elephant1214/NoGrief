package me.elephant1214.nogrief.claims

import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.NoGrief
import org.bukkit.Chunk
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

object ClaimManager {
    private val claimsDir: Path =
        NoGrief.dataDir.resolve("claims").apply { if (!this@apply.exists()) this@apply.createDirectories() }

    private val _claims = mutableSetOf<Claim>()

    fun getClaim(chunk: ClaimChunk): Claim? = this._claims.find { it.containsChunk(chunk) }

    fun getClaim(chunk: Chunk): Claim? = this.getClaim(ClaimChunk(chunk.world, chunk.chunkKey))

    fun addClaim(claim: Claim) = this._claims.add(claim)

    fun removeClaim(claim: Claim) = this._claims.remove(claim)
    
    internal fun saveClaims() {
        try {
            this._claims.forEach { claim ->
                val file = this.claimsDir.resolve("${claim.claimId}.json").createFile()
                NoGrief.JSON.encodeToStream(file, file.outputStream())
            }
        } catch (e: Exception) {
            NoGrief.logger.severe("Failed to save claims: ${e.message}")
        }
    }

    internal fun loadClaims() {
        try {
            this.claimsDir.listDirectoryEntries()
                .filter { it.extension == "json" }
                .map { NoGrief.JSON.decodeFromStream<Claim>(it.inputStream()) }
                .forEach { this.addClaim(it) }
        } catch (e: Exception) {
            NoGrief.logger.severe("Failed to load claims: ${e.message}")
        }
    }

    fun newClaimID(): UUID = UUID.randomUUID()
}
