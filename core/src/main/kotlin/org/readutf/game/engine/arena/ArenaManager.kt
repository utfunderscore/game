package org.readutf.game.engine.arena

import com.github.michaelbull.result.Result
import org.readutf.game.engine.settings.location.PositionData
import kotlin.reflect.KClass

abstract class ArenaManager {

    abstract fun <T : PositionData> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): Result<Arena<T>, Throwable>

    abstract fun freeArena(arena: Arena<*>)

    abstract fun getTemplates(gameType: String): List<ArenaTemplate>
}
