package org.readutf.game.server.game.impl

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.arena.minestom.platform.MinestomItemStack
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.utils.SResult
import org.readutf.game.server.game.GameCreator
import org.readutf.game.server.game.impl.settings.TheBridgePositions
import org.readutf.game.server.game.impl.settings.TheBridgeSettings

class TheBridgeCreator(
    private val arenaManager: ArenaManager<MinestomWorld>,
    private val kitManager: KitManager<MinestomItemStack>,
) : GameCreator<TheBridgeGame> {
    override fun create(): SResult<TheBridgeGame> {
        val arenaResult = arenaManager.loadArena("thebridge", TheBridgePositions::class)
        val arena = arenaResult.getOrElse { return Err(it) }

        return Ok(TheBridgeGame(arena, TheBridgeSettings(), kitManager))
    }
}
