package org.readutf.game.engine.features.respawning

import org.readutf.game.engine.utils.Position

fun interface RespawnHandler {

    fun findRespawnLocation(): Position
}
