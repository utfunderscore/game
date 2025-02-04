package org.readutf.game.engine.arena

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.utils.Position
import org.readutf.game.engine.world.GameWorld
import java.util.UUID

data class Arena<T : PositionData>(
    val arenaId: UUID,
    val instance: GameWorld,
    val positionSettings: T,
    val positions: Map<String, Marker>,
    val size: Position,
    val freeFunc: (Arena<*>) -> Unit,
) {
    fun free() = freeFunc(this)
}
