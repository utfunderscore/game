package org.readutf.game.engine.respawning

import net.minestom.server.entity.Player
import org.readutf.game.engine.types.Result

fun interface RespawnHandler {
    fun getRespawnLocation(player: Player): Result<RespawnPosition>
}
