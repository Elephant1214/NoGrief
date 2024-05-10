package me.elephant1214.nogrief.claims

enum class ClaimPermissionLevel {
    /**
     * Can't interact with anything in a claim, only able to move through it.
     */
    VISITOR,

    /**
     * Can modify all blocks other than containers.
     */
    TRUSTED,

    /**
     * Can modify any blocks and entities in the claim.
     */
    CONTAINER_TRUSTED,

    /**
     * Can modify players' permissions in the claim.
     */
    MANAGER
}