package org.readutf.neolobby.scoreboard

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.concurrent.atomic.AtomicInteger

abstract class Scoreboard {
    val id = idTracker.getAndIncrement()

    abstract fun getTitle(player: Player): Component

    abstract fun getLines(player: Player): List<Component>

    companion object {
        val idTracker = AtomicInteger(0)
    }
}
