package org.readutf.game.engine.features.scoreboard

import net.kyori.adventure.text.Component

interface Scoreboard<T> {
    fun getTitle(player: T): Component

    fun getLines(player: T): List<Component>
}
