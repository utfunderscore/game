package org.readutf.game.engine.arena

import net.minestom.server.instance.Instance
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionSettings
import java.util.UUID

data class Arena<T : PositionSettings>(
    val arenaId: UUID,
    val instance: Instance,
    val positionSettings: T,
    val positions: Map<String, Marker>,
    val freeFunc: (Arena<*>) -> Unit,
) {
    fun free() = freeFunc(this)
}
