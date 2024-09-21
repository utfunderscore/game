package org.readutf.game.engine.settings

import org.readutf.game.engine.settings.location.PositionSettings

class GameSettings<T>(
    val name: String,
    val positionSettings: PositionSettings,
    val gameData: T,
)
