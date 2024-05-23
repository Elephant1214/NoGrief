package me.elephant1214.nogrief.claims.permissions

import kotlinx.serialization.Serializable

@Serializable
enum class ClaimPermission(val permName: String, val description: String) {
    BREAK(BREAK_PERM_NAME, BREAK_PERM_DESC),
    PLACE(PLACE_PERM_NAME, PLACE_PERM_DESC),
    CONTAINERS(CONTAINERS_PERM_NAME, CONTAINERS_PERM_DESC),
    ENTITIES(ENTITIES_PERM_NAME, ENTITIES_PERM_DESC),
    EXPLOSIONS(EXPLOSIONS_PERM_NAME, EXPLOSIONS_PERM_DESC),
    FIRE(FIRE_PERM_NAME, FIRE_PERM_DESC),
    INTERACT(INTERACT_PERM_NAME, INTERACT_PERM_DESC),
    TILE_ENTITIES(TILE_ENTITIES_PERM_NAME, TILE_ENTITIES_PERM_DESC),
    MANAGE(MANAGE_PERM_NAME, MANAGE_PERM_DESC);
}

private const val BREAK_PERM_NAME = "Break"
private const val BREAK_PERM_DESC = "Allows breaking of all blocks other than tile entities and explosives."

private const val PLACE_PERM_NAME = "Place"
private const val PLACE_PERM_DESC = "Allows placing all blocks other than tile entities and explosives."

private const val CONTAINERS_PERM_NAME = "Container Access"
private const val CONTAINERS_PERM_DESC = "Allows accessing all blocks that can hold items."

private const val ENTITIES_PERM_NAME = "Spawn & Attack Entities"
private const val ENTITIES_PERM_DESC = "Allows spawning and attacking of entities."

private const val EXPLOSIONS_PERM_NAME = "Explosions"
private const val EXPLOSIONS_PERM_DESC = "Allows causing explosions and placing blocks that explode."

private const val FIRE_PERM_NAME = "Fire"
private const val FIRE_PERM_DESC = "Allows different methods of starting fires."

private const val INTERACT_PERM_NAME = "Interact"
private const val INTERACT_PERM_DESC =
    "Allows all interactions, such as doors, buttons, levers, entities, etc., other than tile entities."

private const val TILE_ENTITIES_PERM_NAME = "Tile Entities"
private const val TILE_ENTITIES_PERM_DESC = "Allows building, placing, and interacting with tile entities."

private const val MANAGE_PERM_NAME = "Claim Management"
private const val MANAGE_PERM_DESC =
    "Allows a player to manage other players' permissions in claim, but only when the target player does not also have Manage."
