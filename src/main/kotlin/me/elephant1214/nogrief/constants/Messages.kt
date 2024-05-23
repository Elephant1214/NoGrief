package me.elephant1214.nogrief.constants

import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import me.elephant1214.nogrief.players.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Chunk
import org.bukkit.entity.Player

val CANT_DO_THIS_HERE = Component.text("You don't have permission to do this here!", NamedTextColor.RED)
val NO_PISTONS_OUTSIDE_CLAIMS = Component.text("Pistons cannot move outside of claims.", NamedTextColor.YELLOW)

fun Player.sendNoPermission() {
    this.sendActionBar(CANT_DO_THIS_HERE)
}

fun Player.sendPistonMessage() {
    if (!NoGrief.cfg.allowPistonsOutsideOfClaims) {
        this.sendActionBar(NO_PISTONS_OUTSIDE_CLAIMS)
    }
}

val NOT_ENOUGH_CLAIM_CHUNKS = Component.text("You do not have enough claim chunks to do this!", NamedTextColor.RED)
val NOT_IN_CLAIM = Component.text("You must be in a claim to do this!", NamedTextColor.RED)
val NO_PERMISSION = Component.text("You do not have permission to do this!", NamedTextColor.RED)
val ALREADY_CLAIMED = Component.text("This chunk is already claimed!", NamedTextColor.RED)
val NO_CONNECTING_CLAIM = Component.text("No connecting claim managed by you was found!", NamedTextColor.RED)

val DELETE_CLAIM = Component.text("The claim was successfully deleted.", NamedTextColor.GREEN)
val DELETE_CLAIM_NO_CHUNKS =
    Component.text("The claim was deleted as there are no chunks remaining.", NamedTextColor.GREEN)
val REMOVE_CHUNK = Component.text("The chunk was successfully unclaimed.", NamedTextColor.GREEN)
val CREATED_CLAIM = Component.text("A claim was created!", NamedTextColor.GREEN)

const val OWNERSHIP_TRANSFERRED = "You transferred ownership of %s to "
fun Player.sendTransferredOwnership(claim: String, player: Player) {
    this.sendMessage(
        Component.text(OWNERSHIP_TRANSFERRED.format(claim), NamedTextColor.GREEN).append(player.displayName())
    )
}

const val CLAIM_RENAMED = "%s has been renamed to %s"
fun Player.sendRenamed(oldName: String, newName: String) {
    this.sendMessage(Component.text(CLAIM_RENAMED.format(oldName, newName), NamedTextColor.GREEN))
}

const val NEW_OWNER = "You are now the owner of %s"
fun Player.sendNewOwner(claim: String) {
    this.sendMessage(Component.text(NEW_OWNER.format(claim), NamedTextColor.GREEN))
}

const val PERMISSIONS_UPDATED = "Updated the permission %s for "
fun Player.sendPermissionsUpdated(permission: ClaimPermission, target: Player) {
    this.sendMessage(
        Component.text(PERMISSIONS_UPDATED.format(permission.toString()), NamedTextColor.GREEN)
            .append(target.displayName())
    )
}

const val CHUNK_CLAIMED = "Chunk %d, %d was claimed!"
fun Player.sendChunkClaimed(chunk: Chunk) {
    this.sendMessage(Component.text(CHUNK_CLAIMED.format(chunk.x, chunk.z)))
}

const val CLAIM_CHUNK_COUNT = "You currently have %d claim chunks available"
fun Player.sendClaimChunkCount() {
    this.sendMessage(Component.text(CLAIM_CHUNK_COUNT.format(PlayerManager.getPlayer(this).remainingClaimChunks)))
}

val TOGGLED_CLAIM_BYPASSING = Component.text("Toggled claim bypassing ", NamedTextColor.YELLOW)
val ON = Component.text("ON", NamedTextColor.GREEN)
val OFF = Component.text("OFF", NamedTextColor.RED)
fun Player.sendClaimBypassState(state: Boolean) {
    this.sendMessage(TOGGLED_CLAIM_BYPASSING.append(if (state) ON else OFF))
}
