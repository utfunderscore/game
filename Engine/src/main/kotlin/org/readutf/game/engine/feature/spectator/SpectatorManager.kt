package org.readutf.game.engine.feature.spectator

import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.stage.GenericStage
import org.readutf.game.engine.utils.Position
import org.readutf.game.engine.utils.toComponent
import java.util.UUID

abstract class SpectatorManager(
    val platform: Platform<*>,
    val stage: GenericStage,
    val intervals: List<Int> = listOf(15, 10, 5, 4, 3, 2, 1),
    val countdownHandler: CountdownHandler,
) {
    private val spectators = mutableSetOf<UUID>()
    private val spectatorTasks = mutableMapOf<UUID, GameTask>()

    init {

        stage.registerAll(this)

        stage.game.getOnlinePlayers().forEach { player ->

            hideFromParticipants(player)
        }
    }

    abstract fun hideFromParticipants(player: GamePlayer)

    abstract fun showToParticipants(player: GamePlayer)

    fun setSpectator(player: GamePlayer) {
        spectators.add(player.uuid)

        val game = stage.game
        val event = game.eventManager.callEvent(GameSpectateEvent(game, player, Position(0, 0, 0), 5, true), game)

        player.teleport(event.respawnLocation)

        if (event.respawn) {
            val task = stage.schedule(SpectatorTask(game, player, event.respawnTime, this))
            spectatorTasks[player.uuid] = task
        }
    }

    fun removeSpectator(player: GamePlayer) {
        spectators.remove(player.uuid)

        val game = stage.game

        game.spawnPlayer(player).getOrElse {
            player.sendMessage("&cFailed to respawn you, please rejoin the server.".toComponent())
            return
        }

        showToParticipants(player)

        spectatorTasks.remove(player.uuid)
    }

    fun isSpectator(player: GamePlayer): Boolean = spectators.contains(player.uuid)

    fun shutdown() {
        spectators.forEach { player ->
            removeSpectator(platform.getPlayer(player) as GamePlayer)
        }

        spectators.clear()

        spectatorTasks.forEach { (_, task) ->
            stage.game.scheduler.cancelTask(task)
        }

        spectatorTasks.clear()
    }

//    @EventListener
//    fun onDamage(entityDamageEvent: EntityDamageEvent) {
//        val entity = entityDamageEvent.entity
//
//        if (entity is Player && isSpectator(entity)) {
//            entityDamageEvent.isCancelled = true
//        }
//    }
//
//    @EventListener(priority = 0)
//    fun onBlockBreak(e: PlayerBlockBreakEvent) {
//        if (isSpectator(e.player)) {
//            e.isCancelled = true
//        }
//    }

    fun interface CountdownHandler {
        fun onInterval(
            player: GamePlayer,
            interval: Int,
        )
    }
}
