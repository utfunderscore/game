package org.readutf.game.engine.schedular

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.GenericGame

abstract class CountdownTask(
    val game: GenericGame,
    val durationSeconds: Int,
    intervalSeconds: List<Int>,
) : GameTask() {
    private val logger = KotlinLogging.logger { }
    private val activeIntervals = intervalSeconds.toMutableSet()

    override fun tick() {
        val timeLeft = durationSeconds - ((System.currentTimeMillis() - startTime) / 1000)

//        logger.info { "Countdown task for game `${game::class.simpleName}` has $timeLeft seconds left" }

        activeIntervals.filter { timeLeft < it }.forEach { interval ->
            onInterval(interval)
            activeIntervals.remove(interval)
        }

        if (timeLeft < 0) {
            onInterval(0)
            cancel()
        }
    }

    abstract fun onInterval(interval: Int)

    fun isActive(): Boolean = System.currentTimeMillis() - startTime > (durationSeconds * 1000)
}
