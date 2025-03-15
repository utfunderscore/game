package org.readutf.game.engine

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

object GameManager {
    val activeGames = mutableMapOf<String, GenericGame>()
    val playerToGame = mutableMapOf<UUID, GenericGame>()

    fun getGameByPlayer(playerId: UUID): GenericGame? = playerToGame[playerId]

    fun getGameById(gameId: String): GenericGame? = activeGames[gameId]

    fun removeGame(game: Game<*, *>) {
        activeGames.remove(game.gameId)
        game.getPlayers().forEach { player -> playerToGame.remove(player) }
    }

    val tracker = AtomicInteger(1000)

    fun generateGameId(): String = tracker.incrementAndGet().toString()
}
