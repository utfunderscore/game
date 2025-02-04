package org.readutf.game.engine.schedular

abstract class GameTask(
    var startTime: Long = System.currentTimeMillis(),
    var markedForRemoval: Boolean = false,
) {
    abstract fun tick()

    fun markForRemoval() {
        markedForRemoval = true
    }
}

abstract class DelayedGameTask(
    val delay: Long,
) : GameTask()

abstract class RepeatingGameTask(
    val delay: Long,
    val period: Long,
) : GameTask()
