package org.readutf.game.engine.defaults

import io.github.togar2.pvp.events.PlayerExhaustEvent
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.game.engine.stage.Stage

fun Stage.disableDamage() {
    this.registerListener<EntityDamageEvent> {
        it.isCancelled = true
    }
}

fun Stage.disableFoodLoss() {
    this.registerListener<PlayerExhaustEvent> {
        it.isCancelled = true
    }
}
