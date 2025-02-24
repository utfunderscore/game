package org.readutf.game.engine.schedular

abstract class GameTask(
    var startTime: Long = System.currentTimeMillis(),
    var markedForRemoval: Boolean = false,
) {
    abstract fun tick()

    fun markForRemoval() {
        markedForRemoval = true
    }

    fun cancel() = markForRemoval()
}

abstract class DelayedGameTask(
    val delay: Long,
) : GameTask() {

    abstract fun run()

    override fun tick() {
        if (System.currentTimeMillis() - startTime >= delay) {
            run()
            markForRemoval()
        }
    }
}

abstract class RepeatingGameTask(
    val delay: Long,
    val period: Long,
) : GameTask() {

    var lastTick = Long.MAX_VALUE

    abstract fun run()

    override fun tick() {
        val sinceLastTick = System.currentTimeMillis() - lastTick
        val sinceStart = System.currentTimeMillis() - startTime

        if (sinceLastTick >= period) {
            run()
            return
        }

        if (sinceStart >= delay) {
            run()
            lastTick = System.currentTimeMillis()
            return
        }
    }
}
