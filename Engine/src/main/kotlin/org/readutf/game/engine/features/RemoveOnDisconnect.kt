package org.readutf.game.engine.features

import net.minestom.server.event.player.PlayerDisconnectEvent
import org.readutf.game.engine.stage.Stage

fun Stage.removeOnDisconnect() {
    // Remove player from game on disconnect

    registerListener<PlayerDisconnectEvent> { e ->
        game.removePlayer(e.player).onFailure(game::crash)
    }
}
