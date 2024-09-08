package org.readutf.game.engine.game.settings.test

import net.minestom.server.world.Difficulty
import java.util.concurrent.TimeUnit

class DualGameSettings(
    val warmupTime: Long = TimeUnit.SECONDS.toMillis(5),
    val localDifficulty: Difficulty = Difficulty.HARD,
)
