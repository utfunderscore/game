package org.readutf.game.engine.features.respawning

import org.readutf.buildformat.common.markers.Position
import java.util.UUID

fun interface RespawnHandler {

    fun findRespawnLocation(playerId: UUID): Position
}
