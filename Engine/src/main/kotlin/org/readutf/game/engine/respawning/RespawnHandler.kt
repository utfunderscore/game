package org.readutf.game.engine.respawning

import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.utils.SResult

fun interface RespawnHandler {
    fun getRespawnLocation(player: GamePlayer): SResult<RespawnPosition>
}
