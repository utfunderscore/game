package org.readutf.arena.minestom.features

import net.minestom.server.event.player.PlayerDisconnectEvent
import org.readutf.arena.minestom.platform.toArenaPlayer
import org.readutf.game.engine.stage.GenericStage

fun GenericStage.removeOnDisconnect() {
    registerListener<PlayerDisconnectEvent> { e ->
        val player = e.player.toArenaPlayer()
        game.removePlayer(e.player.toArenaPlayer())
    }
}
