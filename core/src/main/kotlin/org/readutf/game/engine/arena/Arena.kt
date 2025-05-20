package org.readutf.game.engine.arena

import org.readutf.buildformat.common.Build
import org.readutf.buildformat.common.format.BuildFormat
import org.readutf.buildformat.common.markers.Marker
import org.readutf.game.engine.world.GameWorld
import java.util.UUID

data class Arena<T : BuildFormat>(
    val buildId: UUID,
    val build: Build,
    val instance: GameWorld,
    val positionSettings: T,
    val positions: List<Marker>,
    val freeFunc: (Arena<*>) -> Unit,
) {
    fun free() = freeFunc(this)
}
