package org.readutf.game.engine.features.spectator

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent

class GameSpectateEvent(
    game: GenericGame,
    val player: Player,
    var respawnLocation: Pos,
    var respawnTime: Int,
    var respawn: Boolean,
) : GameEvent(game)
