package org.readutf.game.server.game.dual.impl

import org.readutf.game.server.game.dual.stages.AwaitingPlayersSettings

data class TheBridgeSettings(
    val awaitingPlayersSettings: AwaitingPlayersSettings = AwaitingPlayersSettings(),
)
