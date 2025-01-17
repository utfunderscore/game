package org.readutf.game.engine.arena.store

import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.utils.SResult

interface ArenaTemplateStore {
    fun save(arenaTemplate: ArenaTemplate): SResult<Unit>

    fun load(name: String): SResult<ArenaTemplate>

    fun loadAllByGameType(gameType: String): List<ArenaTemplate>
}
