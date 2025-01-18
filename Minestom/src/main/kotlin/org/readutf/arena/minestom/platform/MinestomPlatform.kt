package org.readutf.arena.minestom.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.item.ItemStack
import net.minestom.server.timer.TaskSchedule
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.player.ArenaPlayer
import java.util.*

class MinestomPlatform : Platform<MinestomItemStack> {
    override fun getPlayer(uuid: UUID): ArenaPlayer<MinestomItemStack> = PlayerManager.getPlayer(uuid)!!

    override fun scheduleTask(delayTicks: Int, intervalTicks: Int, runnable: Runnable): Runnable {
        MinecraftServer.getSchedulerManager().scheduleTask(runnable, TaskSchedule.tick(delayTicks), TaskSchedule.tick(intervalTicks))
        return runnable
    }

    override fun getAir(): MinestomItemStack = MinestomItemStack(ItemStack.AIR)
}
