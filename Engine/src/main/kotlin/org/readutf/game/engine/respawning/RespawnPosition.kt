package org.readutf.game.engine.respawning

import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.utils.Position

data class RespawnPosition(
    var position: Position,
    var instance: ArenaWorld,
    var safe: Boolean,
)
