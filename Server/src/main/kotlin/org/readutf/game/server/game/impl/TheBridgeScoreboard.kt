package org.readutf.game.server.game.impl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player
import org.readutf.game.engine.utils.plus
import org.readutf.game.engine.utils.toComponent
import org.readutf.neolobby.scoreboard.Scoreboard

class TheBridgeScoreboard(
    val theBridgeGame: TheBridgeGame,
) : Scoreboard {
    override fun getTitle(player: Player): Component = "&9The Bridge".toComponent()

    override fun getLines(player: Player): List<Component> {
        val lines = mutableListOf<Component>()

        lines.add("&7#tb-&7${theBridgeGame.gameId}".toComponent())
        lines.add("".toComponent())

        theBridgeGame.getTeams().map { it as TheBridgeTeam }.forEach { team ->
            val health = theBridgeGame.getTeamHealth(team)
            val heartPart = getHeartLine(health, theBridgeGame.settings.numberOfLives)

            lines.add("[${team.teamName.substring(0, 1).uppercase()}] ".toComponent().color(team.textColor) + heartPart)
        }

        lines.add(" ".repeat(20).toComponent())
        lines.add("&7Kills &f${0}".toComponent())
        lines.add("&7Goals &f${theBridgeGame.getGoals(player)}".toComponent())
        lines.add("".toComponent())
        lines.add("&bexample.com".toComponent())

        return lines
    }

    fun getHeartLine(
        health: Int,
        maxHealth: Int,
    ): Component {
        var hearts = Component.empty()

        repeat(health) {
            val heart = TexturePackManager.fullHeartIcon.toAdventure()

            hearts = hearts.append(heart)
        }

        repeat(maxHealth - health) {
            val heart = TexturePackManager.emptyHeartIcon.toAdventure()

            hearts = hearts.append(heart)
        }

        return hearts.color(TextColor.color(255, 255, 255))
    }
}
