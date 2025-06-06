package org.readutf.engine.minestom.schedular;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.task.GameSchedulerPlatform;

public class MinestomSchedular implements GameSchedulerPlatform {
    @Override
    public void scheduleRepeatingTask(@NotNull Runnable runnable) {
        MinecraftServer.getSchedulerManager().scheduleTask(runnable, TaskSchedule.tick(1), TaskSchedule.tick(1));
    }

    @Override
    public void executeTask(Runnable runnable) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(runnable);
    }
}
