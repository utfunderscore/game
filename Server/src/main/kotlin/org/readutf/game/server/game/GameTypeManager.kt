package org.readutf.game.server.game

import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.server.game.impl.TheBridgeCreator

class GameTypeManager(
    arenaManager: ArenaManager,
    kitManager: KitManager,
) {
    val creators =
        mapOf<String, GameCreator<*>>(
            "thebridge" to TheBridgeCreator(arenaManager, kitManager),
        )

    fun getCreator(type: String) = creators[type]
}
