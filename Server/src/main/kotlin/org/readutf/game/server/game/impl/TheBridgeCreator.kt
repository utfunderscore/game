package org.readutf.game.server.game.impl

import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.GameCreator

class TheBridgeCreator(
    val arenaManager: ArenaManager,
    val kitManager: KitManager,
) : GameCreator<TheBridgeGame> {
    override fun create(): Result<TheBridgeGame> {
        val arenaResult = arenaManager.loadArena("thebridge", TheBridgePositions::class)
        val arena = arenaResult.mapError { return it }

        return TheBridgeGame(arena, TheBridgeSettings(), kitManager).toSuccess()
    }
}
