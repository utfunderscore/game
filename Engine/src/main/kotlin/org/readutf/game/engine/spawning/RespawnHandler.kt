package org.readutf.game.engine.spawning

import java.util.UUID

fun interface RespawnHandler {
    fun respawnPlayer(playerId: UUID)
}
