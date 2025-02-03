package org.readutf.game.server.metrics

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.WriteKotlinApi
import com.influxdb.client.write.Point
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Blocking
import java.time.Instant

class Metric(
    val writerApi: WriteKotlinApi,
    val measurement: String,
) {
    val tags = mutableMapOf<String, String>()

    private var shutdownHook: (() -> Unit)? = null

    @Blocking
    fun write(vararg data: Pair<String, Number>) {
        val point = buildPoint().time(Instant.now(), WritePrecision.NS)
        data.forEach { (key, value) -> point.addField(key, value) }
        runBlocking {
            try {
                writerApi.writePoint(point)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTag(
        key: String,
        value: String,
    ): Metric =
        apply {
            tags[key] = value
        }

    fun shutdown() {
        shutdownHook?.invoke()
    }

    fun addShutdownHook(hook: () -> Unit): Metric =
        apply {
            shutdownHook = hook
        }

    private fun buildPoint(): Point =
        Point
            .measurement(measurement)
            .addTags(tags)
}
