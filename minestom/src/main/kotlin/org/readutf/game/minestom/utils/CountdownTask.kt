package org.readutf.game.minestom.utils

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.game.engine.stage.GenericStage
import java.time.Duration
import java.time.LocalDateTime

class CountdownTask(
    duration: Duration,
    intervals: List<Int>,
    private val intervalExecutor: (Int) -> Unit,
) : RepeatingGameTask(0, 1) {
    private val start: LocalDateTime = LocalDateTime.now()
    private val endTime: LocalDateTime = start.plus(duration)
    private val remainingIntervals = intervals.toMutableList()

    fun getTimeLeft(): Long = Duration.between(LocalDateTime.now(), endTime).toMillis()

    override fun run() {
        if (LocalDateTime.now() >= endTime) {
            intervalExecutor(0)
            markForRemoval()
            return
        }

        val timeLeft = getTimeLeft()
        for (i in remainingIntervals.toList()) {
            if (timeLeft <= i) {
                intervalExecutor(i)
                remainingIntervals.remove(i)
            }
        }
    }
}

fun GenericGame.startCountdown(
    durationMillis: Long,
    intervals: List<Int>,
    intervalExecutor: (Int) -> Unit,
) {
    val duration = Duration.ofMillis(durationMillis)
    val task = CountdownTask(duration, intervals, intervalExecutor)
    schedule(task)
}

fun GenericGame.startCountdown(
    duration: Duration,
    intervals: List<Int>,
    intervalExecutor: (Int) -> Unit,
) {
    val task = CountdownTask(duration, intervals, intervalExecutor)
    schedule(task)
}

fun GenericStage.startCountdown(
    durationMillis: Long,
    intervals: List<Int>,
    intervalExecutor: (Int) -> Unit,
) {
    val duration = Duration.ofMillis(durationMillis)
    val task = CountdownTask(duration, intervals, intervalExecutor)
    schedule(task)
}

fun GenericStage.startCountdown(
    duration: Duration,
    intervals: List<Int>,
    intervalExecutor: (Int) -> Unit,
): CountdownTask {
    val task = CountdownTask(duration, intervals, intervalExecutor)
    schedule(task)
    return task
}
