package org.readutf.game.server.game.dual.impl

import org.readutf.game.engine.Game
import org.readutf.game.server.engine.dual.DualGameSettings
import org.readutf.game.server.game.dual.utils.DualArena

class TheBridgeGame(
    arena: DualArena,
    dualGameSettings: DualGameSettings,
) : Game<DualArena>() {
    init {

        changeArena(arena)
        registerTeam("red")
        registerTeam("blue")
    }
}
