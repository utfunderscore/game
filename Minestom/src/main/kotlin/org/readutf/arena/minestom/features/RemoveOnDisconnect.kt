package org.readutf.arena.minestom.features

import net.minestom.server.event.player.PlayerDisconnectEvent
import org.readutf.game.engine.stage.GenericStage

fun GenericStage.removeOnDisconnect() {
    // Remove player from game on disconnect

    registerListener<PlayerDisconnectEvent> { e ->
        game.removePlayer(e.player).onFailure(game::crash)
    }
}
