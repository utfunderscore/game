package org.readutf.game.engine.respawning

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance

data class RespawnPosition(
    var position: Point,
    var instance: Instance,
    var safe: Boolean,
)
