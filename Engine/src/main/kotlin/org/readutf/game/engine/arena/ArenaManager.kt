package org.readutf.game.engine.arena

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.settings.location.PositionData
import kotlin.reflect.KClass

abstract class ArenaManager {
    private val logger = KotlinLogging.logger {}

    abstract fun <T : PositionData> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): Arena<T>

    abstract fun freeArena(arena: Arena<*>)
}
