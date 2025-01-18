package org.readutf.game.server.game

import org.readutf.arena.minestom.platform.MinestomItemStack
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.server.game.impl.TheBridgeCreator

class GameTypeManager(
    arenaManager: ArenaManager<MinestomWorld>,
    kitManager: KitManager<MinestomItemStack>,
) {
    val creators =
        mapOf<String, GameCreator<*>>(
            "thebridge" to TheBridgeCreator(arenaManager, kitManager),
        )

    fun getCreator(type: String) = creators[type]
}
