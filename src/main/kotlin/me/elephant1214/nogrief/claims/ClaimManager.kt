package me.elephant1214.nogrief.claims

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.utils.toUuid
import org.bukkit.Chunk
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object ClaimManager {
    val claimsDir: Path =
        NoGrief.dataDir.resolve("claims").apply { if (!this@apply.exists()) this@apply.createDirectories() }

    private val claims = mutableSetOf<Claim>()
    private val chunkToClaimMap = mutableMapOf<ClaimChunk, Claim>()

    fun getClaim(chunk: ClaimChunk): Claim? = this.chunkToClaimMap[chunk]

    fun getClaim(chunk: Chunk): Claim? = this.getClaim(ClaimChunk(chunk.world, chunk.chunkKey))

    /**
     * Claim must add its chunks on its own!!!
     */
    fun addClaim(claim: Claim) = this.claims.add(claim)

    fun removeClaim(forRemoval: Claim) {
        this.claims.remove(forRemoval)
        this.chunkToClaimMap.entries.removeIf { it.value == forRemoval }
    }

    fun getChunksForClaim(queryClaim: Claim): List<ClaimChunk> =
        this.chunkToClaimMap.entries.filter { it.value == queryClaim }.map { it.key }

    fun addChunk(chunk: ClaimChunk, claim: Claim) {
        this.chunkToClaimMap[chunk] = claim
    }

    fun removeChunk(chunk: ClaimChunk): Claim? = this.chunkToClaimMap.remove(chunk)

    internal fun loadClaims() {
        this.claimsDir.listDirectoryEntries().filter { it.endsWith(".yml") }.mapNotNull { loadClaim(it) }
            .forEach { this.addClaim(it) }
    }

    private fun loadClaim(ymlFile: Path): Claim? = try {
        val uuid = ymlFile.name.removeSuffix(".yml").toUuid()
        val config = YamlConfiguration.loadConfiguration(ymlFile.toFile())
        Claim.fromYml(uuid, config)
    } catch (e: Exception) {
        null
    }

    fun makeNewClaimID(): UUID = UUID.randomUUID()
}
