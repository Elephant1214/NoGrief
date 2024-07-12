package me.elephant1214.nogrief.hooks.pl3xmap

import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimChunk
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.hooks.utils.getPoly
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.pl3x.map.core.markers.marker.Marker
import net.pl3x.map.core.markers.option.Fill
import net.pl3x.map.core.markers.option.Options
import net.pl3x.map.core.world.World

object Pl3xMapHook {
    fun registerWorld(world: World) {
        world.layerRegistry.register(NoGriefLayer(world))
    }

    fun unloadWorld(world: World) {
        world.layerRegistry.unregister(NoGriefLayer.KEY)
    }

    fun getMarkers(world: World): MutableCollection<Marker<*>> {
        val claimsSet = ClaimManager.getClaims().filter { it.world.name == world.name }.toSet()
        if (claimsSet.isEmpty()) return arrayListOf()

        val groups = convertClaims(claimsSet)
        val markers = arrayListOf<Marker<*>>()

        for (group: MapGroup in groups) {
            val key = String.format("ng_%s_chunk_%s", world.name, group.id)
            markers.add(getPoly(key, group.getChunks()).setOptions(options(group.claim)))
        }
        
        return markers
    }

    private fun options(claim: Claim): Options.Builder {
        val player = claim.getOwnerPlayer()
        val ownerName = player.name ?: "Unknown"

        return Options.builder()
            .strokeColor(claim.color.color.rgb)
            .strokeWeight(3)
            .fillColor(claim.color.color.rgb)
            .fillType(Fill.Type.NONZERO)
            .popupContent(
                """
                Name: ${PlainTextComponentSerializer.plainText().serialize(claim.name)}
                Owner: $ownerName
                """.trimIndent()
            )
    }

    private fun convertClaims(claims: Set<Claim>): List<MapGroup> {
        val combined = arrayListOf<MapGroup>()

        for (claim: Claim in claims) {
            val byOwner = hashMapOf<Claim, MutableList<MapChunk>>()
            for (chunk: ClaimChunk in claim.getChunks()) {
                val list = byOwner.getOrDefault(claim, arrayListOf())
                list.add(MapChunk(chunk.minX(), chunk.minZ(), claim))
                byOwner[claim] = list
            }

            val groups = hashMapOf<Claim, MutableList<MapGroup>>()
            byOwner.forEach { (owner, list) ->

                next@ for (chunk: MapChunk in list) {
                    val groupList = groups.getOrDefault(owner, arrayListOf())
                    for (group: MapGroup in groupList) {
                        if (group.isTouching(chunk)) {
                            group.add(chunk)
                            continue@next
                        }
                    }

                    groupList.add(MapGroup(owner, chunk))
                    groups[owner] = groupList
                }
            }

            groups.values.forEach { list ->
                next@ for (group: MapGroup in list) {
                    for (toChk: MapGroup in combined) {
                        if (toChk.isTouching(group)) {
                            toChk.add(group)
                            continue@next
                        }
                    }

                    combined.add(group)
                }
            }
        }

        return combined
    }
}
