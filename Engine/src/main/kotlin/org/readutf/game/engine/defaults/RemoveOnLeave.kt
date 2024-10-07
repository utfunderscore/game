package org.readutf.game.engine.defaults

import net.minestom.server.event.player.PlayerDisconnectEvent
import org.readutf.game.engine.stage.Stage

fun <T : Stage> T.setRemoveOnLeave() {
    this.registerListener<PlayerDisconnectEvent> {
        game.removePlayer(it.player)
    }
}
