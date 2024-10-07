package org.readutf.game.engine

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

object GameManager {
    val activeGames = mutableMapOf<String, Game<*>>()
    val playerToGame = mutableMapOf<UUID, Game<*>>()

    fun getGameByPlayer(playerId: UUID): Game<*>? = playerToGame[playerId]

    fun getGameById(gameId: String): Game<*>? = activeGames[gameId]

    val tracker = AtomicInteger(1000)

    fun generateGameId(): String = tracker.incrementAndGet().toString()
}
