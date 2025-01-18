package org.readutf.arena.minestom.features

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import org.readutf.arena.minestom.platform.MinestomPlayer
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.event.impl.GameLeaveEvent
import org.readutf.game.engine.stage.GenericStage

fun GenericStage.playerJoinMessage(supplier: (Player) -> Component) {
    registerListener<GameJoinEvent> {
        it.game.messageAll(supplier((it.player as MinestomPlayer).player))
    }
}

fun GenericStage.playerLeaveMessage(supplier: (Player) -> Component) {
    registerListener<GameLeaveEvent> {
        it.game.messageAll(supplier((it.player as MinestomPlayer).player))
    }
}
