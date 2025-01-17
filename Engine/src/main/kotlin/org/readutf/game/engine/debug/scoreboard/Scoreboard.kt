package org.readutf.neolobby.scoreboard

import net.kyori.adventure.text.Component
import org.readutf.game.engine.platform.player.ArenaPlayer

interface Scoreboard {
    fun getTitle(player: ArenaPlayer<*>): Component

    fun getLines(player: ArenaPlayer<*>): List<Component>
}
