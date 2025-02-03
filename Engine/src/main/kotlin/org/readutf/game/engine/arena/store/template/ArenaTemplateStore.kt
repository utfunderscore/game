package org.readutf.game.engine.arena.store.template

import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.types.Result

interface ArenaTemplateStore {
    fun save(arenaTemplate: ArenaTemplate): Result<Unit>

    fun load(name: String): Result<ArenaTemplate>

    fun loadAllByGameType(gameType: String): List<ArenaTemplate>
}
