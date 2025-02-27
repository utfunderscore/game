package org.readutf.game.engine.features.respawning

import org.readutf.game.engine.utils.Position
import java.util.UUID

fun interface RespawnHandler {

    fun findRespawnLocation(playerId: UUID): Position
}
