package org.readutf.game.server.game.dual.stages

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.arena.minestom.features.*
import org.readutf.arena.minestom.features.combat.setDamageRule
import org.readutf.arena.minestom.features.combat.setFoodLossRule
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.respawning.impl.registerTeamIdSpawnHandler
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.game.engine.settings.GameSettings
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.dual.DualGamePositions

class AwaitingPlayersStage<ARENA : Arena<*, MinestomWorld>, TEAM : GameTeam>(
    override val game: Game<MinestomWorld, ARENA, TEAM>,
    previousStage: Stage<MinestomWorld, ARENA, TEAM>?,
    val settings: AwaitingPlayersSettings,
    val arenaPositions: DualGamePositions,
) : Stage<MinestomWorld, ARENA, TEAM>(game, previousStage) {
    val countDownTask = CountdownTask(listOf(60, 30, 15, 10, 5, 4, 3, 2, 1))

    init {
        registerTeamIdSpawnHandler("spawn") { 0 }
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
                endStage().getOrElse { game.crash(Err(it)) }
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
                    endStage().getOrElse { game.crash(Err(it)) }
                    return
                }

                unsentAlerts.toList().filter { it > timeLeft }.forEach {
                    game.messageAll("Game starting in $it seconds".toComponent())
                    unsentAlerts.remove(it)
                }
            }
        }
    }

    class Creator<ARENA : Arena<*, MinestomWorld>, TEAM : GameTeam>(
        val settings: AwaitingPlayersSettings,
        val dualGamePositions: DualGamePositions,
    ) : StageCreator<MinestomWorld, ARENA, TEAM> {
        override fun create(
            game: Game<MinestomWorld, ARENA, TEAM>,
            previousStage: Stage<MinestomWorld, ARENA, TEAM>?,
        ): SResult<Stage<MinestomWorld, ARENA, TEAM>> = Ok(AwaitingPlayersStage(game, previousStage, settings, dualGamePositions))
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
