package me.elephant1214.nogrief.claims.listeners

import me.elephant1214.nogrief.claims.ClaimManager
import me.elephant1214.nogrief.constants.sendCantDoThisHere
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object InteractListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractTile(event: PlayerInteractEvent) {
        // TODO: Handle cases where players can't open a container but are crouching and
        //  right clicking it to build if someone complains
        if (!event.action.isRightClick) return
        val block = event.clickedBlock ?: return
        val claim = ClaimManager.getClaim(block.chunk) ?: return
        if (block.state is Container && claim.canAccessContainers(event.player)) return
        if (block.state is TileState && claim.hasTilePerm(event.player)) return
        if (block.type.toString().uppercase().contains("ANVIL") && claim.hasTilePerm(event.player)) return
        event.isCancelled = true
        event.player.sendCantDoThisHere()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractNonTile(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val claim = ClaimManager.getClaim(block.chunk) ?: return
        if (!event.action.isLeftClick) return

        val clickedStack = when (event.hand!!) {
            EquipmentSlot.HAND -> event.player.inventory.itemInMainHand
            EquipmentSlot.OFF_HAND -> event.player.inventory.itemInOffHand
            else -> return
        }

        // Ignore fireworks
        if (clickedStack.type == Material.FIREWORK_ROCKET) return

        if (clickedStack.type.toString().uppercase().contains("AXE") && !claim.canBreak(event.player)) {
            event.isCancelled = true
            event.player.sendCantDoThisHere()
            return
        }

        if (!clickedStack.type.toString().uppercase().contains("BOAT")
            && !clickedStack.type.toString().uppercase().contains("MINECART")
        ) return
        if (claim.hasEntitiesPerm(event.player)) return

        if (claim.canInteract(event.player)) return
        event.isCancelled = true
        event.player.sendCantDoThisHere()
    }
}
