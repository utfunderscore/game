package org.readutf.neolobby.scoreboard

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

interface Scoreboard {
    fun getTitle(player: Player): Component

    fun getLines(player: Player): List<Component>
}
