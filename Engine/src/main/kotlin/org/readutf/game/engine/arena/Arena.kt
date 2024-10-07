package org.readutf.game.engine.arena

import com.google.gson.annotations.Expose
import net.minestom.server.instance.Instance
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionData
import java.util.UUID

data class Arena<T : PositionData>(
    @Expose val arenaId: UUID,
    val instance: Instance,
    @Expose val positionSettings: T,
    @Expose val positions: Map<String, Marker>,
    val freeFunc: (Arena<*>) -> Unit,
) {
    fun free() = freeFunc(this)
}
