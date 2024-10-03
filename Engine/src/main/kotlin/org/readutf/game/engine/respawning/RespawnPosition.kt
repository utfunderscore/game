package org.readutf.game.engine.respawning

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance

data class RespawnPosition(
    val position: Point,
    val instance: Instance,
    val safe: Boolean,
)
