package org.readutf.game.engine.features

import io.github.togar2.pvp.events.PlayerExhaustEvent
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.game.engine.stage.Stage

/**
 * Disables combat in the stage when [allowDamage] returns false.
 */
fun Stage.setDamageRule(allowDamage: () -> Boolean = { true }) {
    this.registerListener<EntityDamageEvent> {
        it.isCancelled = !allowDamage()
    }
}

fun Stage.setFoodLossRule(allowFoodLoss: () -> Boolean = { true }) {
    this.registerListener<PlayerExhaustEvent> {
        it.isCancelled = !allowFoodLoss()
    }
}
