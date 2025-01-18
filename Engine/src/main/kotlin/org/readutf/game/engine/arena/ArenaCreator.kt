package org.readutf.game.engine.arena

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.settings.location.PositionData
import java.util.*

interface ArenaCreator<WORLD : ArenaWorld> {

    fun <POSITION : PositionData> create(
        arenaId: UUID,
        positionSettings: POSITION,
        arenaWorld: WORLD,
        positions: Map<String, Marker>,
    ): Arena<POSITION, WORLD>
}
