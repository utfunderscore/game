package org.readutf.game.server.commands

import com.github.michaelbull.result.getOrElse
import net.minestom.server.entity.Player
import org.readutf.arena.minestom.platform.toArenaPlayer
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.commands.completions.GameCompleter
import org.readutf.game.server.commands.completions.GameTypeCompleter
import org.readutf.game.server.game.GameTypeManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named

class GameCommand(
    private val gameTypeManager: GameTypeManager,
) {
    @Command("game start")
    fun startGame(
        player: Player,
        @GameTypeCompleter type: String,
    ) {
        val creator =
            gameTypeManager.getCreator(type) ?: let {
                player.sendMessage("&cGame type $type not found".toComponent())
                return
            }

        val game =
            creator.create().getOrElse {
                player.sendMessage("&cFailed to create game: $it".toComponent())
                return
            }

        game.start().getOrElse {
            player.sendMessage("&cFailed to start game: $it".toComponent())
            return
        }

        player.sendMessage("&aGame created with id ${game.gameId}".toComponent())
    }

    @Command("game add <gameid> <target>")
    fun addPlayer(
        player: Player,
        @Named("gameid") @GameCompleter gameId: String,
        target: Player,
    ) {
        val game =
            GameManager.getGameById(gameId) ?: let {
                player.sendMessage("&cGame not found".toComponent())
                return
            }

        if (game.getPlayers().contains(target.uuid)) {
            player.sendMessage("&cYou are already in the game".toComponent())
            return
        }

        val result =
            game.getTeams().minByOrNull { it.players.size } ?: let {
                player.sendMessage("&cNo teams available".toComponent())
                return
            }

        game.addPlayer(target.toArenaPlayer(), result).getOrElse {
            player.sendMessage("&cFailed to add player: $it".toComponent())
        }

        player.sendMessage("&aPlayer added to game".toComponent())
    }
}
