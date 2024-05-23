package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.sendNoPermission
import me.elephant1214.nogrief.constants.sendPistonMessage
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Piston
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object BlockListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk) ?: return
        val isTile = event.block.state is TileState
        if (!claim.canBreak(event.player) || (isTile && !claim.hasTilePerm(event.player))) {
            event.isCancelled = true
            event.player.sendNoPermission()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockDamage(event: BlockDamageEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk) ?: return
        if (claim.canBreak(event.player)) return
        event.isCancelled = true
        event.player.sendNoPermission()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        val claim = ClaimManager.getClaim(event.entity.chunk) ?: return
        val remover = event.remover
        if (remover is Player && (!claim.canBreak(remover) || !claim.hasEntitiesPerm(remover))) {
            event.isCancelled = true
            remover.sendNoPermission()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPlayerTrample(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL || event.clickedBlock == null) return
        val claim = ClaimManager.getClaim(event.clickedBlock!!.chunk) ?: return
        if (event.clickedBlock!!.type == Material.FARMLAND && !claim.canBreak(event.player)) {
            event.isCancelled = true
            event.player.sendNoPermission()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk)
        if (claim == null) {
            if (event.blockPlaced.blockData is Piston) {
                event.player.sendPistonMessage()
            }
            return
        }
        val isTile = event.block.state is TileState
        if (!claim.canPlace(event.player) || (isTile && !claim.hasTilePerm(event.player))) {
            event.isCancelled = true
            event.player.sendNoPermission()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockMultiPlace(event: BlockMultiPlaceEvent) {
        event.replacedBlockStates.forEach { state ->
            val claim = ClaimManager.getClaim(event.block.chunk) ?: return@forEach
            val isTile = state is TileState
            if (!claim.canPlace(event.player) || (isTile && !claim.hasTilePerm(event.player))) {
                event.isCancelled = true
                event.player.sendNoPermission()
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onFluidPlace(event: PlayerInteractEvent) {
        this.onFluid(event)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onFluidRemove(event: PlayerInteractEvent) {
        this.onFluid(event, false)
    }

    private fun onFluid(event: PlayerInteractEvent, placed: Boolean = true) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = if (event.clickedBlock!!.blockData is Waterlogged) {
            event.clickedBlock!!
        } else {
            event.clickedBlock!!.getRelative(event.blockFace)
        }

        val claim = ClaimManager.getClaim(block.chunk) ?: return
        if (placed && claim.canPlace(event.player)) return

        val usedItem = when (event.hand) {
            EquipmentSlot.HAND -> event.player.inventory.itemInMainHand
            EquipmentSlot.OFF_HAND -> event.player.inventory.itemInOffHand
            else -> return
        }
        if (usedItem.type.name.uppercase().contains("BUCKET")) {
            event.isCancelled = true
            event.player.sendNoPermission()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        onPiston(event, event.blocks)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        onPiston(event, event.blocks)
    }

    private fun onPiston(event: BlockPistonEvent, blocks: List<Block>) {
        val pistonClaim = ClaimManager.getClaim(event.block.chunk)
        if (pistonClaim == null) {
            event.isCancelled = true
            return
        }

        blocks.forEach { block ->
            val claim = ClaimManager.getClaim(block.chunk)
            if (claim == null || pistonClaim != claim) {
                event.isCancelled = true
                return@onPiston
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        val claim = ClaimManager.getClaim(event.block.chunk) ?: return
        if (event.ignitingEntity is Player) {
            val player = event.ignitingEntity as Player
            if (claim.hasFirePerm(player) && (event.block.type != Material.TNT || claim.hasExplosionPerm(player))) return
            player.sendNoPermission()
        } else if (event.ignitingEntity is Arrow) {
            val arrow = event.ignitingEntity as Arrow
            val player = if (arrow.shooter is Player) {
                arrow.shooter as Player
            } else if (arrow.shooter is Skeleton && (arrow.shooter as Skeleton).target is Player) {
                (arrow.shooter as Skeleton).target as Player
            } else {
                null
            }

            if (player == null || claim.hasExplosionPerm(player)) return
            /* Don't send a no permission message if the check fails as the arrow hitting
            could still be accidental */
        } else {
            NoGrief.logger.fine(event.dumpEvent())
            return
        }
        event.isCancelled = true
    }
}

private fun BlockIgniteEvent.dumpEvent(): String = buildString {
    appendLine("Unhandled block ignite case, dumping event:")
    appendLine("Igniting entity: ${this@dumpEvent.ignitingEntity}")
    appendLine("Igniting block: ${this@dumpEvent.ignitingBlock}")
    appendLine("Block: ${this@dumpEvent.block}")
    append("Cause: ${this@dumpEvent.cause}")
}
