package org.readutf.game.engine.game.settings

import org.readutf.game.engine.game.settings.location.PositionSettings

class GameSettings<T>(
    val name: String,
    val positionSettings: PositionSettings,
    val gameData: T,
)
