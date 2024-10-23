package org.readutf.game.server.metrics.tasks

import net.minestom.server.MinecraftServer
import net.minestom.server.event.instance.InstanceRegisterEvent
import net.minestom.server.event.instance.InstanceUnregisterEvent
import net.minestom.server.instance.Instance
import org.readutf.game.engine.utils.addListener
import org.readutf.game.server.metrics.MetricsManager
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class InstanceMetrics(
    val metricsManager: MetricsManager,
) {
    private val executor = Executors.newSingleThreadScheduledExecutor()

    val instanceTrackers = mutableMapOf<UUID, MutableList<TaskMetric>>()

    val instanceCreateListener =
        Consumer<InstanceRegisterEvent> { event ->
            createInstanceTracker(event.instance, "instance_player_count", 1000) { instance -> instance.players.size }
            createInstanceTracker(event.instance, "instance_chunks", 1000) { instance -> instance.chunks.size }
            createInstanceTracker(event.instance, "instance_entities", 1000) { instance -> instance.entities.size }
        }

    val instanceRemoveListener =
        Consumer<InstanceUnregisterEvent> {
            val instance = it.instance
            val id = instance.uniqueId
            val tasks = instanceTrackers.remove(id) ?: emptyList()

            tasks.forEach { task ->
                task.shutdown()
                metricsManager.unregisterMetric(task.metric)
            }
        }

    init {
        MinecraftServer.getGlobalEventHandler().addListener(instanceCreateListener)
        MinecraftServer.getGlobalEventHandler().addListener(instanceRemoveListener)
    }

    private fun createInstanceTracker(
        instance: Instance,
        name: String,
        delayMillis: Long = 1000,
        task: (Instance) -> Int,
    ) {
        val metric = createMetric(name, instance)
        val taskMetric =
            TaskMetric(
                metric,
                executor,
                delayMillis,
                TimeUnit.MILLISECONDS,
            ) { task.invoke(instance) }

        instanceTrackers
            .getOrPut(instance.uniqueId) { mutableListOf() }
            .add(taskMetric)
    }

    private fun createMetric(
        name: String,
        instance: Instance,
    ) = metricsManager
        .registerMetric(name)
        .addTag("instance", instance.uniqueId.toString())
}
