package org.readutf.game.engine

import org.readutf.game.engine.types.Result
import java.util.UUID

object GameManager {
    val activeGames = mutableListOf<Game<*>>()
    val playerToGame = mutableMapOf<UUID, Game<*>>()

    fun startGame(game: Game<*>): Result<Unit> {
        game.start().mapError { return it }

        activeGames.add(game)
        game.getPlayers().forEach { player ->
            playerToGame[player] = game
        }

        return Result.empty()
    }
}
