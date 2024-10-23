package org.readutf.game.engine.features.spectator

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.utils.toComponent

class SpectatorManager(
    val stage: Stage,
    val intervals: List<Int> = listOf(15, 10, 5, 4, 3, 2, 1),
    val countdownHandler: CountdownHandler,
) {
    val spectators = mutableSetOf<Player>()
    val spectatorTasks = mutableMapOf<Player, GameTask>()

    init {

        stage.registerAll(this)

        stage.game.getOnlinePlayers().forEach { player ->

            player.isAutoViewable = true

            player.updateViewableRule {
                return@updateViewableRule player !in spectators
            }
        }
    }

    fun setSpectator(player: Player) {
        spectators.add(player)

        val game = stage.game
        val event = GameEventManager.callEvent(GameSpectateEvent(game, player, Pos.ZERO, 5, true), game)

        player.isAllowFlying = true
        player.isFlying = true
        player.heal()

        player.teleport(event.respawnLocation)

        player.updateViewableRule()

        if (event.respawn) {
            val task = stage.schedule(SpectatorTask(game, player, event.respawnTime, this))
            spectatorTasks[player] = task
        }
    }

    fun removeSpectator(player: Player) {
        spectators.remove(player)

        val game = stage.game

        game.spawnPlayer(player).onFailure {
            player.sendMessage("&cFailed to respawn you, please rejoin the server.".toComponent())
        }

        player.updateViewableRule()
        spectatorTasks.remove(player)
    }

    fun isSpectator(player: Player): Boolean = spectators.contains(player)

    fun shutdown() {
        spectators.forEach { player ->
            removeSpectator(player)
        }

        spectators.clear()

        spectatorTasks.forEach { (_, task) ->
            stage.game.scheduler.cancelTask(task)
        }

        spectatorTasks.clear()
    }

    @EventListener
    fun onDamage(entityDamageEvent: EntityDamageEvent) {
        val entity = entityDamageEvent.entity

        if (entity is Player && isSpectator(entity)) {
            entityDamageEvent.isCancelled = true
        }
    }

    @EventListener(priority = 0)
    fun onBlockBreak(e: PlayerBlockBreakEvent) {
        if (isSpectator(e.player)) {
            e.isCancelled = true
        }
    }

    fun interface CountdownHandler {
        fun onInterval(
            player: Player,
            interval: Int,
        )
    }
}
