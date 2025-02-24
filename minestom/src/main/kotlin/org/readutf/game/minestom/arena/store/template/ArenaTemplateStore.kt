package org.readutf.game.minestom.arena.store.template

import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.utils.SResult

interface ArenaTemplateStore {
    fun save(arenaTemplate: ArenaTemplate): SResult<Unit>

    fun load(name: String): SResult<ArenaTemplate>

    fun findByGameType(gameType: String): List<ArenaTemplate>
}
