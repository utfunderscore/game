package org.readutf.game.engine.features

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.event.impl.GameLeaveEvent
import org.readutf.game.engine.stage.Stage

fun Stage.playerJoinMessage(supplier: (Player) -> Component) {
    registerListener<GameJoinEvent> {
        it.game.messageAll(supplier(it.player))
    }
}

fun Stage.playerLeaveMessage(supplier: (Player) -> Component) {
    registerListener<GameLeaveEvent> {
        it.game.messageAll(supplier(it.player))
    }
}
