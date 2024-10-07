package org.readutf.game.engine

import java.util.UUID

object GameManager {
    val activeGames = mutableListOf<Game<*>>()
    val playerToGame = mutableMapOf<UUID, Game<*>>()

    fun getGameByPlayer(playerId: UUID): Game<*>? = playerToGame[playerId]

    fun getGameById(gameId: String): Game<*>? =
        activeGames
            .firstOrNull { game -> game.gameId.toString().startsWith(gameId) }
}
