package org.readutf.game.engine.arena

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.settings.location.PositionData
import java.util.UUID

typealias GenericArena = Arena<*, *>

abstract class Arena<POSITION : PositionData, WORLD : ArenaWorld>(
    val arenaId: UUID,
    val positionSettings: POSITION,
    val arenaWorld: WORLD,
    val positions: Map<String, Marker>,
    private val freeFunc: (Arena<POSITION, WORLD>) -> Unit,
) {

    fun free() = freeFunc(this)
}
