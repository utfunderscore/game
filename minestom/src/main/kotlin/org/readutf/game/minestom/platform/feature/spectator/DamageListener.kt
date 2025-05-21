package org.readutf.game.minestom.platform.feature.spectator

import java.util.*

fun interface DamageListener {
    fun onDamage(
        playerId: UUID,
        finalDamage: Float,
        isCancelled: Boolean,
    )
}
