package org.readutf.game.engine.features.combat

import io.github.togar2.pvp.events.PlayerExhaustEvent
import io.github.togar2.pvp.events.PlayerRegenerateEvent
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.game.engine.stage.GenericStage

/**
 * Disables combat in the stage when [allowDamage] returns false.
 */
fun GenericStage.setDamageRule(allowDamage: () -> Boolean = { true }) {
    this.registerListener<EntityDamageEvent> {
        it.isCancelled = !allowDamage()
    }
}

fun GenericStage.setFoodLossRule(allowFoodLoss: () -> Boolean = { true }) {
    this.registerListener<PlayerExhaustEvent> {
        it.isCancelled = !allowFoodLoss()
    }
}

fun GenericStage.disableNaturalRegen() {
    registerListener<PlayerRegenerateEvent> {
        it.isCancelled = true
    }
}
