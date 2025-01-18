package org.readutf.game.server.metrics.tasks

import net.minestom.server.MinecraftServer
import net.minestom.server.event.server.ServerTickMonitorEvent
import org.readutf.game.engine.utils.addListener
import org.readutf.game.server.metrics.MetricsManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class ServerMetrics(
    metricsManager: MetricsManager,
) {
    private val tickTimeMetric = metricsManager.registerMetric("tick_time")
    private val tickExecutor = Executors.newSingleThreadScheduledExecutor()

    val lockObject = Any()
    var tickTimeSum: Double = 0.0
    var tickTimeMeasurements = 0

    val tickListener =
        Consumer<ServerTickMonitorEvent> { e ->
            synchronized(lockObject) {
                tickTimeSum += e.tickMonitor.tickTime
                tickTimeMeasurements++
            }
        }

    init {
        MinecraftServer.getGlobalEventHandler().addListener(tickListener)

        TaskMetric(tickTimeMetric, tickExecutor, 1, TimeUnit.SECONDS) {
            synchronized(lockObject) {
                val avg = tickTimeSum / (tickTimeMeasurements.toDouble())
                tickTimeSum = 0.0
                tickTimeMeasurements = 0
                avg
            }
        }
    }
}
