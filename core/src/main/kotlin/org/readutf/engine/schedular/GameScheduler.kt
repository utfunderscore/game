package org.readutf.game.engine.schedular

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.stage.GenericStage

abstract class GameScheduler {
    private val logger = KotlinLogging.logger { }

    private val gameTasks = mutableMapOf<String, MutableList<GameTask>>()

    init {
        scheduleTask {

            val copy = gameTasks.toMap()
            gameTasks.clear()

            for ((gameId, tasks) in copy) {
                for (task in tasks) {
                    if (task.markedForRemoval) return@scheduleTask
                    task.tick()
                    if (!task.markedForRemoval) {
                        gameTasks.getOrPut(gameId) { mutableListOf() }.add(task)
                    }
                }
            }
        }
    }

    abstract fun scheduleTask(runnable: Runnable)

    fun schedule(game: GenericGame, gameTask: GameTask) {
        logger.info { "Scheduling task $gameTask" }

        gameTasks.getOrPut(game.gameId) { mutableListOf() }.add(gameTask)
    }

    fun schedule(stage: GenericStage, gameTask: GameTask) {
        logger.info { "Scheduling task $gameTask" }

        val task = object : GameTask() {
            override fun tick() {
                if (stage.game.currentStage == stage) {
                    gameTask.tick()
                }
            }
        }
        schedule(stage.game, task)
    }

    fun cancelGameTasks(game: GenericGame) {
        logger.info { "Cancelling tasks for game ${game.gameId}" }
        gameTasks[game.gameId]?.forEach { it.markedForRemoval = true }
    }
}
