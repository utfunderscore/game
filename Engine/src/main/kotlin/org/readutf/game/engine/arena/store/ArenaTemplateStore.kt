package org.readutf.game.engine.arena.store

import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.types.Result

interface ArenaTemplateStore {
    fun saveArenaTemplate(arenaTemplate: ArenaTemplate): Result<Unit>

    fun loadArenaTemplate(name: String): Result<ArenaTemplate>

    fun loadAllByGameType(gameType: String): Result<List<ArenaTemplate>>
}
