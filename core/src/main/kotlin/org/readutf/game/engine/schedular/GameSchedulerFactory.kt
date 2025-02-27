package org.readutf.game.engine.schedular

import org.readutf.game.engine.GenericGame

fun interface GameSchedulerFactory {
    fun build(game: GenericGame): GameScheduler
}
