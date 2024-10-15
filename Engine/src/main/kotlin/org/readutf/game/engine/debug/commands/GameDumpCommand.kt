package org.readutf.game.engine.debug.commands

import com.google.gson.GsonBuilder
import net.minestom.server.entity.Player
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.utils.toComponent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional

class GameDumpCommand {
    @Command("gamedev dump <gameid>")
    fun dumpState(
        player: Player,
        @Optional gameid: String?,
    ) {
        val game: GenericGame =
            if (gameid == null) {
                GameManager.getGameByPlayer(player.uuid)
            } else {
                GameManager.getGameById(gameid)
            } ?: let {
                player.sendMessage("&cCould not the game".toComponent())
                return
            }

        val gson =
            GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create()

        val jsonString = gson.toJson(game)

        println(jsonString)

        jsonString.split("\\n").forEach { line ->
            player.sendMessage(line.toComponent())
        }
    }
}
