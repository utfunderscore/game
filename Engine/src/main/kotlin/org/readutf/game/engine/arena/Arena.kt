package org.readutf.game.engine.arena

import net.minestom.server.instance.Instance
import java.util.UUID

data class Arena(
    val arenaId: UUID,
    val instance: Instance,
    val arenaTemplate: ArenaTemplate,
)
