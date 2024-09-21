package org.readutf.game.engine.respawning

import net.minestom.server.instance.Instance
import org.readutf.game.engine.types.Position

data class RespawnPosition(
    val position: Position,
    val instance: Instance,
    val safe: Boolean,
)
