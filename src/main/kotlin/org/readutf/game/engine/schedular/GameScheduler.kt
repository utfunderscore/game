package org.readutf.game.engine.schedular

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.stage.GenericStage

abstract class GameScheduler(
    val game: GenericGame,
) {
    private val logger = KotlinLogging.logger { }

    private val globalTasks = mutableSetOf<GameTask>()

    private val stageTasks = mutableMapOf<GenericStage, MutableSet<GameTask>>()

    init {
        logger.info { "Starting scheduler" }
    }

    fun schedule(
        stage: GenericStage,
        gameTask: GameTask,
    ) {
        startTask {
            globalTasks.removeIf { it.markedForRemoval }
            globalTasks.forEach(::tickTask)

            if (game.currentStage == null) return@startTask

            stageTasks.forEach { (stage, tasks) ->
                tasks.removeIf { it.markedForRemoval }

                if (stage == game.currentStage) {
                    tasks.forEach(::tickTask)
                }
            }
        }

        gameTask.startTime = System.currentTimeMillis()

        logger.info { "Scheduling task `${gameTask::class.simpleName}` for stage `${stage::class.simpleName}`" }
        stageTasks.getOrPut(stage) { mutableSetOf() }.add(gameTask)
    }

    fun cancelTask(gameTask: GameTask) {
        globalTasks.filter { it == gameTask }.forEach { it.markForRemoval() }
        stageTasks.values
            .forEach { tasks ->
                tasks
                    .filter { task ->
                        task == gameTask
                    }.forEach {
                        it.markForRemoval()
                    }
            }
    }

    abstract fun startTask(runnable: Runnable)

    private fun tickTask(task: GameTask) {
        if (task is RepeatingGameTask) {
            if (System.currentTimeMillis() - task.startTime >= task.delay) {
                task.tick()
            }
        } else if (task is DelayedGameTask) {
            if (System.currentTimeMillis() - task.startTime >= task.delay) {
                task.tick()
                task.markForRemoval()
            }
        } else {
            task.tick()
        }
    }

    fun schedule(
        stage: GenericStage,
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
        stage: GenericStage,
        delay: Long,
        runnable: () -> Unit,
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
        delay: Long,
        runnable: () -> Unit,
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
        stage: GenericStage,
        delay: Long,
        period: Long,
        runnable: () -> Unit,
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
        delay: Long,
        period: Long,
        runnable: () -> Unit,
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
