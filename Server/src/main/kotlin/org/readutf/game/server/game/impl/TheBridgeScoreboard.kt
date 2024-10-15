package org.readutf.game.server.game.impl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player
import org.readutf.game.engine.utils.plus
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.dev.TexturePackManager
import org.readutf.neolobby.scoreboard.Scoreboard

class TheBridgeScoreboard(
    val theBridgeGame: TheBridgeGame,
) : Scoreboard {
    override fun getTitle(player: Player): Component = "&9The Bridge".toComponent()

    override fun getLines(player: Player): List<Component> =
        listOf(
            "&7#tb-&7${theBridgeGame.gameId}".toComponent(),
            "".toComponent(),
            "&c[R] ".toComponent() + getHeartLine(5, 5),
            "&9[B] ".toComponent() + getHeartLine(5, 5),
            " ".repeat(20).toComponent(),
            "&7Kills ${1}".toComponent(),
            "&7Goals ${1}".toComponent(),
            "".toComponent(),
            "&bexample.com".toComponent(),
        )

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
