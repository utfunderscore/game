package org.readutf.game.server.metrics

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import org.readutf.game.server.metrics.tasks.InstanceMetrics
import org.readutf.game.server.metrics.tasks.ServerMetrics
import java.util.*
import java.util.concurrent.Executors

class MetricsManager(
    val serverId: UUID,
    influxToken: String,
    org: String,
    bucket: String,
) {
    private val token = influxToken
    private val client = InfluxDBClientKotlinFactory.create("http://influxdb2:8086", token.toCharArray(), org, bucket)
    private val writer = client.getWriteKotlinApi()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val metrics = mutableListOf<Metric>()

    init {
        val result = kotlin.runCatching { client.ping() }
        if (result.isFailure || result.getOrNull() == false) {
            throw result.exceptionOrNull() ?: IllegalStateException("Failed to connect to influxdb")
        }

        InstanceMetrics(this)
        ServerMetrics(this)
    }

    fun registerMetric(measurement: String): Metric =
        Metric(writer, measurement).addTag("server", serverId.toString()).apply {
            metrics.add(this)
        }

    fun unregisterMetric(metric: Metric) {
        metrics.remove(metric)
        metric.shutdown()
    }

    fun shutdown() {
        scheduler.shutdown()
        metrics.forEach(Metric::shutdown)
        client.close()
    }
}
