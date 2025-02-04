package org.readutf.game.engine.schedular

fun interface GameSchedulerFactory {
    fun build(): GameScheduler
}
