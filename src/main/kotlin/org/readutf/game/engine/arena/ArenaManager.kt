package org.readutf.game.engine.arena

import org.readutf.game.engine.settings.location.PositionData
import kotlin.reflect.KClass

abstract class ArenaManager {

    abstract fun <T : PositionData> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): Arena<T>

    abstract fun freeArena(arena: Arena<*>)

    abstract fun getTemplates(): List<ArenaTemplate>
}
