package me.elephant1214.nogrief.hooks.squaremap.task

import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.hooks.squaremap.utils.getPoly
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import xyz.jpenilla.squaremap.api.BukkitAdapter
import xyz.jpenilla.squaremap.api.Key.key
import xyz.jpenilla.squaremap.api.MapWorld
import xyz.jpenilla.squaremap.api.SimpleLayerProvider
import xyz.jpenilla.squaremap.api.marker.MarkerOptions

class SquaremapTask private constructor(
    private val world: World,
    private val provider: SimpleLayerProvider,
) : BukkitRunnable() {
    private var stop: Boolean = false
    
    constructor(world: MapWorld, provider: SimpleLayerProvider) : this(BukkitAdapter.bukkitWorld(world), provider)

    override fun run() {
        if (this.stop) {
            this.cancel()
        }
        this.updateClaims()
    }
    
    private fun updateClaims() {
        this.provider.clearMarkers()
        
        ClaimManager.getClaims().forEach { claim ->
            if (claim.world != this.world) return
            drawClaim(claim)
        }
    }
    
    private fun drawClaim(claim: Claim) {
        val polygon = getPoly(claim.getChunks().toList())
        val options = options(claim)
        polygon.markerOptions(options)
        
        val markerKey = key("nogrief_claim_${claim.claimId}")
        this.provider.addMarker(markerKey, polygon)
    }
    
    private fun options(claim: Claim): MarkerOptions.Builder {
        val ownerName = if (claim.owner == ClaimManager.ADMIN_CLAIM_OWNER) {
            "Admin"
        } else {
            claim.getPlayerOwner().name ?: "Unknown"
        }
        return MarkerOptions.builder()
            .strokeColor(claim.color.color)
            .strokeWeight(2)
            .strokeOpacity(0.75)
            .fillColor(claim.color.color)
            .fillOpacity(0.2)
            .clickTooltip("Owner: $ownerName")
            .hoverTooltip(PlainTextComponentSerializer.plainText().serialize(claim.name))
    }
    
    fun disable() {
        this.cancel()
        this.stop = true
        this.provider.clearMarkers()
    }
}
