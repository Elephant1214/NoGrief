package me.elephant1214.nogrief.menus

import me.elephant1214.inventorymenus.InventoryMenus.menu
import me.elephant1214.inventorymenus.menu.InventoryMenu
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.locale.LocaleManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object PermissionsMenu {
    private const val PERMISSIONS_MENU_SIZE = 27
    private const val PLAYER_HEAD_SLOT = 4

    private val permissionItems = listOf(
        Triple(ClaimPermission.BREAK, Material.WOODEN_PICKAXE, NamedTextColor.RED),
        Triple(ClaimPermission.PLACE, Material.GRASS_BLOCK, NamedTextColor.GREEN),
        Triple(ClaimPermission.CONTAINERS, Material.CHEST, NamedTextColor.YELLOW),
        Triple(ClaimPermission.ENTITIES, Material.SHEEP_SPAWN_EGG, NamedTextColor.WHITE),
        Triple(ClaimPermission.EXPLOSIONS, Material.TNT, NamedTextColor.RED),
        Triple(ClaimPermission.FIRE, Material.FLINT_AND_STEEL, TextColor.color(Color.ORANGE.asRGB())),
        Triple(ClaimPermission.INTERACT, Material.STONE_BUTTON, NamedTextColor.GRAY),
        Triple(ClaimPermission.TILE_ENTITIES, Material.ENCHANTING_TABLE, NamedTextColor.AQUA),
        Triple(ClaimPermission.MANAGE, Material.BEACON, NamedTextColor.DARK_PURPLE),
    )

    fun permissionsMenu(manager: Player, claim: Claim, target: OfflinePlayer) {
        manager.menu(
            NoGrief,
            manager,
            Component.text(target.name!!, TextColor.color(claim.color.color.rgb))
                .append(Component.text("'s Permissions", TextColor.color(claim.color.color.rgb))),
            PERMISSIONS_MENU_SIZE,
            { true },
        ) {
            this.slot(PLAYER_HEAD_SLOT, makeHead(target, claim))

            this@PermissionsMenu.permissionItems.forEachIndexed { index, (permission, item, color) ->
                this@PermissionsMenu.addPermissionItem(
                    9 + index,
                    this@menu,
                    claim,
                    manager,
                    target,
                    permission,
                    item,
                    color,
                )
            }

            this.fill(ItemStack(Material.GRAY_STAINED_GLASS_PANE))

            this.onClose {
                this.player.sendMessage(
                    LocaleManager.get(
                        "claim.permissions.updated",
                        Placeholder.component("player", Component.text(target.name!!))
                    )
                )
            }
        }
    }

    private fun makeHead(player: OfflinePlayer, claim: Claim): ItemStack {
        val stack = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = stack.itemMeta as SkullMeta
        meta.owningPlayer = player
        meta.displayName(Component.text(player.name!!, TextColor.color(claim.color.color.rgb)))
        stack.setItemMeta(meta)
        return stack
    }

    private fun addPermissionItem(
        slot: Int,
        menu: InventoryMenu,
        claim: Claim,
        manager: Player,
        player: OfflinePlayer,
        permission: ClaimPermission,
        item: Material,
        color: TextColor,
    ) {
        val stack = ItemStack(item, 1)
        val meta = stack.itemMeta
        meta.displayName(Component.text(permission.permName, color))

        var currentState = claim.getPermission(player, permission)
        meta.lore(makeLore(permission.description, claim.color.color.rgb, currentState))
        stack.setItemMeta(meta)

        menu.slot(slot, stack) {
            currentState = claim.getPermission(player, permission)
            claim.setPermission(player, permission, !currentState)
            this@slot.inventory.setItem(this@slot.slot, stack.apply {
                meta.lore(makeLore(permission.description, claim.color.color.rgb, !currentState))
                this@apply.setItemMeta(meta)
            })
            manager.playSound(manager, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
        }
    }

    private fun makeLore(description: String, color: Int, currentState: Boolean): List<Component> =
        mutableListOf<Component>().apply {
            addAll(splitDesc(description))
            add(Component.text(""))
            add(
                Component.text("Current: ", TextColor.color(color))
                    .append(LocaleManager.get(if (!currentState) "allow" else "deny"))
            )
        }

    private fun splitDesc(text: String, maxLength: Int = 24): List<Component> {
        val words = text.split(" ")
        val components = mutableListOf<Component>()
        var line = StringBuilder()

        words.forEach { word ->
            if (line.length + word.length > maxLength) {
                components.add(Component.text(line.toString().trim(), NamedTextColor.GRAY))
                line = StringBuilder()
            }
            line.append("$word ")
        }

        if (line.isNotEmpty()) {
            components.add(Component.text(line.toString().trim(), NamedTextColor.GRAY))
        }

        return components
    }
}
