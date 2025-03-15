package org.readutf.game.minestom.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import org.readutf.game.engine.schedular.GameScheduler

object MinestomScheduler : GameScheduler() {
    override fun scheduleTask(runnable: Runnable) {
        MinecraftServer.getSchedulerManager().scheduleTask(runnable, TaskSchedule.tick(1), TaskSchedule.tick(1))
    }
}
