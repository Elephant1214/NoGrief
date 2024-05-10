package me.elephant1214.nogrief.claims

import org.bukkit.entity.Player

sealed class ClaimAction(val claim: Claim, val actor: Player)

class ChunkClaimedAction(claim: Claim, actor: Player, claimChunk: ClaimChunk) : ClaimAction(claim, actor)

class ChunkUnclaimedAction(claim: Claim, actor: Player, claimChunk: ClaimChunk) : ClaimAction(claim, actor)

class PlayerDemotedAction(claim: Claim, actor: Player, demoted: Player) : ClaimAction(claim, actor)

class PlayerPromotedAction(claim: Claim, actor: Player, promoted: Player) : ClaimAction(claim, actor)
