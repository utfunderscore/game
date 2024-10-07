package org.readutf.game.server.game.dual.impl

import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.GameCreator
import org.readutf.game.server.game.dual.DualGamePositions

class TheBridgeCreator(
    val arenaManager: ArenaManager,
) : GameCreator<TheBridgeGame> {
    override fun create(): Result<TheBridgeGame> {
        val arenaResult = arenaManager.loadArena("thebridge", DualGamePositions::class)
        val arena = arenaResult.mapError { return it }

        return TheBridgeGame(arena, TheBridgeSettings()).toSuccess()
    }
}
