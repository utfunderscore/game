package org.readutf.game.engine.features.spectator

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.utils.toComponent

class SpectatorManager(
    val stage: Stage,
    val intervals: List<Int> = listOf(15, 10, 5, 4, 3, 2, 1),
    val countdownHandler: CountdownHandler,
) {
    val spectators = mutableSetOf<Player>()

    init {
        stage.game.getOnlinePlayers().forEach { player ->

            player.isAutoViewable = true

//            player.updateViewerRule {
//                val entity = it
//                if (entity is Player) {
//                    return@updateViewerRule !spectators.contains(entity)
//                }
//                return@updateViewerRule true
//            }

            player.updateViewableRule {
                return@updateViewableRule player !in spectators
            }
        }
    }

    fun setSpectator(player: Player) {
        spectators.add(player)

        val game = stage.game
        val event = GameEventManager.callEvent(GameSpectateEvent(game, player, Pos.ZERO, 15, true), game)

        player.isAllowFlying = true
        player.isFlying = true

        player.teleport(event.respawnLocation)

        player.updateViewableRule()

        stage.schedule(SpectatorTask(game, player, event.respawnTime, this))
    }

    fun removeSpectator(player: Player) {
        spectators.remove(player)

        val game = stage.game

        game.spawnPlayer(player).onFailure {
            player.sendMessage("&cFailed to respawn you, please rejoin the server.".toComponent())
        }

        player.updateViewableRule()
    }

    fun isSpectator(player: Player): Boolean = spectators.contains(player)

    fun interface CountdownHandler {
        fun onInterval(
            player: Player,
            interval: Int,
        )
    }
}
