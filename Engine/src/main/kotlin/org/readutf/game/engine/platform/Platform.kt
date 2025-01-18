package org.readutf.game.engine.platform

import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.platform.player.ArenaPlayer
import java.util.UUID

interface Platform<T : ArenaItemStack<T>> {

    fun getPlayer(uuid: UUID): ArenaPlayer<T>?

    fun scheduleTask(delayTicks: Int, intervalTicks: Int, runnable: Runnable): Runnable

    fun getAir(): T
}
