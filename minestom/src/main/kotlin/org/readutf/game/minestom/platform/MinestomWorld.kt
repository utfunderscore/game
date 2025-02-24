package org.readutf.game.minestom.platform

import net.minestom.server.instance.Instance
import org.readutf.game.engine.world.GameWorld

data class MinestomWorld(
    val instance: Instance,
) : GameWorld
