package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame

class GameDeathEvent(
    game: GenericGame,
    val player: Player,
)
