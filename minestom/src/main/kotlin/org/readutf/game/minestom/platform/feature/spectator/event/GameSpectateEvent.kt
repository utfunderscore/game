package org.readutf.game.minestom.platform.feature.spectator.event

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.minestom.platform.feature.spectator.SpectatorData

class GameSpectateEvent(
    game: GenericGame,
    var spectatorData: SpectatorData,
) : GameEvent(game)
