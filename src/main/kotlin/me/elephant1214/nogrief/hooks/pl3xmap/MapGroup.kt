package me.elephant1214.nogrief.hooks.pl3xmap

import me.elephant1214.nogrief.claims.Claim

class MapGroup(
    val claim: Claim,
    chunk: MapChunk
) {
    private val _chunks = arrayListOf<MapChunk>()
    val id = "${chunk.minX}_${chunk.minZ}"
    
    init {
        this._chunks.add(chunk)
    }
    
    fun isTouching(chunk: MapChunk): Boolean {
        for (toChk: MapChunk in this._chunks) {
            if (toChk.isTouching(chunk)) {
                return true
            }
        }
        return false
    }
    
    fun isTouching(group: MapGroup): Boolean {
        for (chunk: MapChunk in group._chunks) {
            if (isTouching(chunk)) {
                return true
            }
        }
        return false
    }
    
    fun add(chunk: MapChunk) {
        this._chunks.add(chunk)
    }
    
    fun add(claim: MapGroup) {
        this._chunks.addAll(claim._chunks)
    }
    
    fun getChunks(): List<MapChunk> = this._chunks.toList()
}
