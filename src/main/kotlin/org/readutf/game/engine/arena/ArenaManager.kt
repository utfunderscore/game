package org.readutf.game.engine.arena

import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.utils.SResult
import kotlin.reflect.KClass

abstract class ArenaManager {

    abstract fun <T : PositionData> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): SResult<Arena<T>>

    abstract fun freeArena(arena: Arena<*>)

    abstract fun getTemplates(): List<ArenaTemplate>
}
