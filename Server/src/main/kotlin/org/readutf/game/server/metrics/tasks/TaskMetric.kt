package org.readutf.game.server.metrics.tasks

import org.readutf.game.server.metrics.Metric
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TaskMetric(
    val metric: Metric,
    scheduledExecutorService: ScheduledExecutorService,
    period: Long,
    unit: TimeUnit,
    private val measure: () -> Number,
) {
    val task: ScheduledFuture<*> =
        scheduledExecutorService.scheduleAtFixedRate({
            metric.write(
                "count" to measure(),
            )
        }, 1, period, unit)

    fun shutdown() {
        task.cancel(false)
    }
}
