package org.readutf.game.engine

import java.util.UUID

object GameManager {
    val activeGames = mutableListOf<Game<*>>()
    val playerToGame = mutableMapOf<UUID, Game<*>>()

    fun startGame(game: Game<*>) {
        game.start()

        activeGames.add(game)
        game.getPlayers().forEach { player ->
            playerToGame[player] = game
        }
    }
}
