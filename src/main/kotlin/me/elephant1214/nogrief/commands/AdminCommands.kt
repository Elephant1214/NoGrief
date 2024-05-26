package me.elephant1214.nogrief.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import me.elephant1214.ccfutils.annotations.BetterCmdPerm
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.constants.ADMIN
import me.elephant1214.nogrief.constants.CLAIM_BYPASS
import me.elephant1214.nogrief.locale.Locale
import me.elephant1214.nogrief.locale.LocaleManager
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
object AdminCommands {
    @CommandMethod("nogrief reload [resource]")
    @BetterCmdPerm(ADMIN, permDefault = PermissionDefault.OP)
    @CommandDescription("Reloads the specified config.")
    fun reload(
        sender: Player, @Argument("resource", defaultValue = "all") resource: Resource
    ) {
        when (resource) {
            Resource.MAIN -> {
                NoGrief.reloadCfg()
                sender.sendMessage(
                    LocaleManager.get(
                        "admin.reload.specific",
                        Placeholder.component("config", Component.text("${Resource.MAIN}", NamedTextColor.AQUA))
                    )
                )
            }

            Resource.LOCALE -> {
                LocaleManager.reload()
                sender.sendMessage(LocaleManager.get("admin.reload.specific"))
                sender.sendMessage(
                    LocaleManager.get(
                        "admin.reload.specific",
                        Placeholder.component("config", Component.text("${Resource.LOCALE}", NamedTextColor.AQUA))
                    )
                )
            }

            Resource.ALL -> {
                NoGrief.reloadConfig()
                LocaleManager.reload()
                sender.sendMessage(LocaleManager.get("admin.reload.all"))
            }
        }
    }

    enum class Resource {
        MAIN, LOCALE, ALL,
    }

    @CommandMethod("nogrief locale <locale>")
    @BetterCmdPerm(ADMIN, permDefault = PermissionDefault.OP)
    @CommandDescription("Changes the current locale.")
    fun setLocale(
        sender: Player, @Argument("locale") locale: Locale
    ) {
        LocaleManager.changeLocale(locale)
        sender.sendMessage(
            LocaleManager.get(
                "admin.localeChanged",
                Placeholder.component("new", Component.text(locale.toString(), NamedTextColor.GREEN))
            )
        )
    }

    // @CommandMethod("nogrief admin claiming")
    // @BetterCmdPerm(ADMIN, permDefault = PermissionDefault.OP)
    // @CommandDescription("Toggles admin claiming.")
    // fun adminClaiming(
    //     sender: Player,
    // ) {
    //     LocaleManager.changeLocale(locale)
    // }
    // 
    // @CommandMethod("nogrief admin forcedelete")
    // @BetterCmdPerm(ADMIN, permDefault = PermissionDefault.OP)
    // @CommandDescription("Forcefully deletes a claim.")
    // fun forceDelete(
    //     sender: Player,
    // ) {
    //     LocaleManager.changeLocale(locale)
    // }

    @CommandMethod("nogrief bypass")
    @BetterCmdPerm(CLAIM_BYPASS, permDefault = PermissionDefault.OP)
    @CommandDescription("Toggles claim bypassing.")
    fun claimBypass(
        sender: Player,
    ) {
        val newState = !PlayerManager.inBypassClaimMode(sender)
        PlayerManager.setBypassClaimMode(sender, newState)
        sender.sendMessage(
            LocaleManager.get(
                "admin.claimBypass",
                Placeholder.component("state", if (newState) LocaleManager.get("on") else LocaleManager.get("off"))
            )
        )
    }
}
