package org.readutf.game.engine.platform.world

import org.readutf.game.engine.utils.Position

interface ArenaWorld {
    fun getNearbyEntities(position: Position, range: Double): Any
}
