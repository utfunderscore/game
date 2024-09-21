package org.readutf.game.server.game

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.respawning.impl.registerTeamIdSpawnHandler
import org.readutf.game.engine.settings.test.DualGamePositions
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.GameBuilder

object DevelopmentGame {
    fun createDevelopmentGame(
        player: Player,
        arenaManager: ArenaManager,
    ): Game<Arena<DualGamePositions>> {
        val arena = arenaManager.loadArena("test", DualGamePositions::class).onFailure { throw Exception(it) }

        val game =
            GameBuilder {
                changeArena(arena)
                registerTeamIdSpawnHandler("spawn")
                addTeam(GameTeam(player.uuid))
            }.build()

        GameManager.startGame(game)

        return game
    }
}
