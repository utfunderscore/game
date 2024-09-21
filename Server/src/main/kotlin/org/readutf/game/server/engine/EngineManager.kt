package org.readutf.game.server.engine

import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.test.DualGamePositions
import org.readutf.game.engine.settings.test.DualGameSettings
import java.io.File

class EngineManager {
    val gameSettingsManager = GameSettingsManager(File(System.getenv("user.dir")))

    init {
        gameSettingsManager.registerGameDefaults("NoDebuff", DualGameSettings(), DualGamePositions::class)
    }
}
