package org.readutf.game.engine.schedular

import com.google.gson.annotations.Expose
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.stage.Stage

class GameScheduler(
    val game: GenericGame,
) {
    private val logger = KotlinLogging.logger { }

    @Expose private val globalTasks = mutableSetOf<GameTask>()

    @Expose private val stageTasks = mutableMapOf<Stage, MutableSet<GameTask>>()

    private var task: Task? = null

    init {
        logger.info { "Starting scheduler" }
    }

    fun schedule(
        stage: Stage,
        gameTask: GameTask,
    ) {
        if (task == null) task = startTask()

        gameTask.startTime = System.currentTimeMillis()

        logger.info { "Scheduling task `${gameTask::class.simpleName}` for stage `${stage::class.simpleName}`" }
        stageTasks.getOrPut(stage) { mutableSetOf() }.add(gameTask)
    }

    fun cancelTask(gameTask: GameTask) {
        globalTasks.remove(gameTask)
        stageTasks.values.forEach { it.remove(gameTask) }
    }

    fun startTask() =
        MinecraftServer.getSchedulerManager().scheduleTask({
            globalTasks.forEach(::tickTask)

            if (game.currentStage == null) return@scheduleTask

            stageTasks[game.currentStage]?.forEach(::tickTask)
        }, TaskSchedule.tick(1), TaskSchedule.tick(1))

    private fun tickTask(task: GameTask) {
        if (task is RepeatingGameTask) {
            if (System.currentTimeMillis() - task.startTime >= task.delay) {
                task.tick()
            }
        } else if (task is DelayedGameTask) {
            if (System.currentTimeMillis() - task.startTime >= task.delay) {
                task.tick()
                globalTasks.remove(task)
            }
        } else {
            task.tick()
        }
    }

    fun schedule(
        stage: Stage,
        task: () -> Unit,
    ) {
        schedule(
            stage,
            object : DelayedGameTask(0) {
                override fun tick() {
                    task()
                }
            },
        )
    }

    fun schedule(runnable: () -> Unit) {
        schedule(
            object : DelayedGameTask(0) {
                override fun tick() {
                    runnable()
                }
            },
        )
    }

    fun schedule(
        stage: Stage,
        runnable: () -> Unit,
        delay: Long,
    ) {
        schedule(
            stage,
            object : DelayedGameTask(delay) {
                override fun tick() {
                    runnable()
                }
            },
        )
    }

    fun schedule(
        runnable: () -> Unit,
        delay: Long,
    ) {
        schedule(
            object : DelayedGameTask(delay) {
                override fun tick() {
                    runnable()
                }
            },
        )
    }

    fun schedule(
        stage: Stage,
        runnable: () -> Unit,
        delay: Long,
        period: Long,
    ) {
        schedule(
            stage,
            object : RepeatingGameTask(delay, period) {
                override fun tick() {
                    runnable()
                }
            },
        )
    }

    fun schedule(
        runnable: () -> Unit,
        delay: Long,
        period: Long,
    ) {
        schedule(
            object : RepeatingGameTask(delay, period) {
                override fun tick() {
                    runnable()
                }
            },
        )
    }

    fun schedule(gameTask: GameTask) {
        logger.info { "Scheduling task $gameTask" }
        globalTasks.add(gameTask)
    }
}
