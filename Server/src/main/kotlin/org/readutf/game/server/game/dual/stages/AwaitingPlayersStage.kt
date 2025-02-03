package org.readutf.game.server.game.dual.stages

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.features.*
import org.readutf.game.engine.features.combat.setDamageRule
import org.readutf.game.engine.features.combat.setFoodLossRule
import org.readutf.game.engine.respawning.impl.registerTeamIdSpawnHandler
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.game.engine.settings.GameSettings
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.dual.DualGamePositions

class AwaitingPlayersStage<ARENA : Arena<*>, TEAM : GameTeam>(
    override val game: Game<ARENA, TEAM>,
    previousStage: Stage<ARENA, TEAM>?,
    val settings: AwaitingPlayersSettings,
    val arenaPositions: DualGamePositions,
) : Stage<ARENA, TEAM>(game, previousStage) {
    val countDownTask = CountdownTask(listOf(60, 30, 15, 10, 5, 4, 3, 2, 1))

    init {
        registerTeamIdSpawnHandler("spawn")
        game.scheduler.schedule(this, countDownTask)
        removeOnDisconnect()
        setDamageRule { false }
        setFoodLossRule { false }
        setBlockBreakRule { _, _, _ -> false }
        setBlockPlaceRule { _, _, _ -> false }

        playerJoinMessage {
            settings.playerJoinMessage
                .replace("{player}", it.username)
                .replace("{players}", game.getOnlinePlayers().count().toString())
                .replace("{max}", settings.maxPlayers.toString())
                .toComponent()
        }

        playerLeaveMessage {
            settings.playerLeaveMessage
                .replace("{player}", it.username)
                .replace("{players}", (game.getOnlinePlayers().count()).toString())
                .replace("{max}", settings.maxPlayers.toString())
                .toComponent()
        }
    }

    @EventListener
    fun onGameJoin(gameJoinEvent: GameJoinEvent) {
        if (game.getOnlinePlayers().count() >= settings.maxPlayers) {
            gameJoinEvent.setCancelled(true)
        }
    }

    inner class CountdownTask(
        intervalAlerts: List<Int>,
    ) : RepeatingGameTask(1000, 50) {
        private val unsentAlerts: MutableList<Int> =
            intervalAlerts
                .filter { it < settings.playersReachedCountdown }
                .toMutableList()

        private var sinceLastMinPlayers = 0L
        private var sinceLastCountdown = 0L

        override fun tick() {
            if (settings.minStartPlayers <= 0) {
                endStage().onFailure(game::crash)
                return
            }

            if (game.getOnlinePlayers().count() < settings.minStartPlayers) {
                sinceLastCountdown = 0

                // Not enough players to start match, send message to all players every 15 seconds
                if (System.currentTimeMillis() - sinceLastMinPlayers > 1000 * 15) {
                    sinceLastMinPlayers = System.currentTimeMillis()
                    game.messageAll("Waiting for more players to join...".toComponent())
                }

                return
            } else {
                if (sinceLastCountdown == 0L) sinceLastCountdown = System.currentTimeMillis()

                val sinceStarted = System.currentTimeMillis() - sinceLastCountdown
                val timeLeft = 5 - (sinceStarted / 1000)

                if (timeLeft < 0) {
                    endStage().onFailure(game::crash)
                    return
                }

                unsentAlerts.toList().filter { it > timeLeft }.forEach {
                    game.messageAll("Game starting in $it seconds".toComponent())
                    unsentAlerts.remove(it)
                }
            }
        }
    }

    class Creator<ARENA : Arena<*>, TEAM : GameTeam>(
        val settings: AwaitingPlayersSettings,
        val dualGamePositions: DualGamePositions,
    ) : StageCreator<ARENA, TEAM> {
        override fun create(
            game: Game<ARENA, TEAM>,
            previousStage: Stage<ARENA, TEAM>?,
        ): Result<Stage<ARENA, TEAM>> = Result.success(AwaitingPlayersStage(game, previousStage, settings, dualGamePositions))
    }
}

class AwaitingPlayersSettings(
    val maxPlayers: Int = 2,
    val minStartPlayers: Int = 2,
    val playersReachedCountdown: Int = 15,
    val gameStartingMessage: String = "&7Game starting in &9{time} seconds",
    val awaitingPlayers: String = "&7Waiting for &9{players} &7more players...",
    val playerJoinMessage: String = "&9{player} &7has joined the game &b({players}/{max})",
    val playerLeaveMessage: String = "&9{player} &7has left the game &b({players}/{max})",
) : GameSettings()
