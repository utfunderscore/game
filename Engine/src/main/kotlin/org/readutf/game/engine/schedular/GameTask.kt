package org.readutf.game.engine.schedular

abstract class GameTask(
    val startTime: Long = System.currentTimeMillis(),
) {
    abstract fun tick()
}

abstract class DelayedGameTask(
    val delay: Long,
) : GameTask()

abstract class RepeatingGameTask(
    val delay: Long,
    val period: Long,
) : GameTask()
