package org.readutf.game.server.commands

import net.minestom.server.entity.Player
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.commands.complter.GameCompleter
import org.readutf.game.server.game.GameTypeManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named

class GameCommand(
    val gameTypeManager: GameTypeManager,
) {
    @Command("game start")
    fun startGame(
        player: Player,
        type: String,
    ) {
        val creator =
            gameTypeManager.getCreator(type) ?: let {
                player.sendMessage("&cGame type $type not found".toComponent())
                return
            }

        val game =
            creator.create().onFailure {
                player.sendMessage("&cFailed to create game: ${it.getError()}".toComponent())
                return
            }

        game.start().onFailure {
            player.sendMessage("&cFailed to start game: ${it.getError()}".toComponent())
            return
        }

        player.sendMessage("&aGame created with id ${game.gameId}".toComponent())
    }

    @Command("game add <gameid> <target>")
    fun addPlayer(
        player: Player,
        target: Player,
        @Named("gameid") @GameCompleter gameId: String,
    ) {
        val game =
            GameManager.getGameById(gameId) ?: let {
                player.sendMessage("&cGame not found".toComponent())
                return
            }

        if (game.getPlayers().contains(player.uuid)) {
            player.sendMessage("&cYou are already in the game".toComponent())
            return
        }

        val result =
            game.getTeams().minByOrNull { it.players.size } ?: let {
                player.sendMessage("&cNo teams available".toComponent())
                return
            }

        game.addPlayer(target, result).onFailure {
            player.sendMessage("&cFailed to add player: ${it.getError()}".toComponent())
            return
        }

        player.sendMessage("&aPlayer added to game".toComponent())
    }
}
