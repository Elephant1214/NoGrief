package me.elephant1214.nogrief.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.utils.horizontalFaces
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Container
import org.bukkit.block.data.type.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.util.BoundingBox
import java.util.*

object BlockListeners : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun blockBreak(event: BlockBreakEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk)
        if (claim != null && (!claim.canModifyBlocks(event.player) || (event.block is Container && !claim.canModifyContainers(
                event.player
            )))
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun signChange(event: SignChangeEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk)
        if (claim != null && !claim.canModifyBlocks(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun blockMultiPlace(event: BlockMultiPlaceEvent) {
        val anyNotAllowed = event.replacedBlockStates.any { block ->
            !isBlockPlacementAllowed(block, event.player)
        }

        if (anyNotAllowed) event.isCancelled = true
    }

    private fun isBlockPlacementAllowed(block: BlockState, player: Player): Boolean {
        val claim = ClaimManager.getClaim(block.chunk)
        return claim != null && claim.canModifyBlocks(player)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun blockPlace(event: BlockPlaceEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk)
        if (claim != null && !claim.canModifyBlocks(event.player)) {
            event.isCancelled = true
            return
        }

        testAndDenyDoubleChestPlacement(event.block, event.player)
    }

    private fun testAndDenyDoubleChestPlacement(block: Block, player: Player) {
        if (block.blockData !is Chest) return

        horizontalFaces().forEach { face ->
            val relative = block.getRelative(face)
            if (relative.blockData !is Chest) return@forEach

            val relativeClaim = ClaimManager.getClaim(relative.chunk)
            if (relativeClaim?.canModifyContainers(player) == false) {
                setSingleChest(block)
                setSingleChest(relative)
                player.sendBlockChange(relative.location, relative.blockData)
            }
        }
    }

    private fun setSingleChest(block: Block) {
        val chest = block.blockData as Chest
        chest.type = Chest.Type.SINGLE
        block.blockData = chest
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun pistonExtend(event: BlockPistonExtendEvent) {
        pistonMove(event, event.blocks, false)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun pistonRetract(event: BlockPistonRetractEvent) {
        pistonMove(event, event.blocks, true)
    }
    
    private fun pistonMove(event: BlockPistonEvent, blocks: List<Block>, retract: Boolean) {
        val direction = event.direction
        val piston = event.block
        val pistonClaim = ClaimManager.getClaim(piston.chunk)
        
        if (pistonClaim != null && blocks.isEmpty()) {
            if (retract) return
            
            val movedBlock = piston.getRelative(direction)
            val claim = ClaimManager.getClaim(piston.chunk)
            if (claim != null && pistonClaim.owner == claim.owner) {
                event.isCancelled = true
            }
            return
        }
        
        val movedBlock = BoundingBox.of()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun tntPrime(event: TNTPrimeEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk) ?: return
        if ((event.primingEntity is Player && !claim.canModifyBlocks(event.primingEntity as Player))) {
            event.isCancelled = true
            return
        }
        if ((event.primingEntity != null && !claim.containsChunk(event.primingEntity!!.chunk))) {
            event.isCancelled = true
            return
        }
    }
}