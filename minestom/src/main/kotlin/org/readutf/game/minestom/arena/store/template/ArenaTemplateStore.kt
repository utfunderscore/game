package org.readutf.game.minestom.arena.store.template

import com.github.michaelbull.result.Result
import org.readutf.game.engine.arena.ArenaTemplate

interface ArenaTemplateStore {
    fun save(arenaTemplate: ArenaTemplate): Result<Unit, Throwable>

    fun load(name: String): Result<ArenaTemplate, Throwable>

    fun findByGameType(gameType: String): List<ArenaTemplate>
}
