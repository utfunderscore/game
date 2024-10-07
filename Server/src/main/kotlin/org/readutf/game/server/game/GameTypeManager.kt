package org.readutf.game.server.game

import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.server.game.dual.impl.TheBridgeCreator

class GameTypeManager(
    arenaManager: ArenaManager,
) {
    private val creators =
        mapOf<String, GameCreator<*>>(
            "thebridge" to TheBridgeCreator(arenaManager),
        )

    fun getCreator(type: String) = creators[type]
}
