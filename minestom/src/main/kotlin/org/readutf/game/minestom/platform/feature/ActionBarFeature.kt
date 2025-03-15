package org.readutf.game.minestom.platform.feature

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.game.minestom.utils.getOnline

class ActionBarFeature(
    val genericGame: GenericGame,
    val textProvider: (Player) -> Component,
) : Feature() {
    override fun getTasks(): List<GameTask> = listOf(ActionBarTask())

    inner class ActionBarTask : RepeatingGameTask(0, 1) {
        private val previousTex = mutableMapOf<Player, Component>()

        override fun run() {
            for (player in genericGame.getOnline()) {
                val newLine = textProvider.invoke(player)
                val previousLine = previousTex[player] ?: Component.empty()

                if (newLine != previousLine) {
                    player.sendActionBar(newLine)
                    previousTex[player] = newLine
                }
            }
        }
    }

    override fun shutdown() {
    }
}
