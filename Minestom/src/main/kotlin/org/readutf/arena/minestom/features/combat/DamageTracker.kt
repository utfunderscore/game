package org.readutf.arena.minestom.features.combat

import io.github.togar2.pvp.events.FinalDamageEvent
import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.impl.GameDeathEvent
import org.readutf.game.engine.event.listener.RegisteredListener
import org.readutf.game.engine.event.listener.TypedGameListener
import java.util.UUID

class DamageTracker(
    game: GenericGame,
) {
    private val lastDamager = mutableMapOf<UUID, Pair<UUID, Long>>()

    val lastDamageListener =
        TypedGameListener<FinalDamageEvent> {
            val attacker = it.damage.attacker ?: return@TypedGameListener
            lastDamager[it.entity.uuid] = Pair(attacker.uuid, System.currentTimeMillis())
        }

    init {
        game.eventManager.registerListener(
            game,
            FinalDamageEvent::class,
            RegisteredListener(
                gameListener = lastDamageListener,
                ignoreCancelled = false,
                ignoreSpectators = false,
                priority = 1,
            ),
        )

        // Clear deaths when GameDeathEvent occurs
        game.eventManager.registerListener(
            game,
            GameDeathEvent::class,
            RegisteredListener(
                gameListener =
                TypedGameListener<GameDeathEvent> {
                    lastDamager.remove(it.player.uuid)
                },
                ignoreCancelled = false,
                ignoreSpectators = false,
                priority = 50,
            ),
        )
    }

    fun getLastDamager(player: Player): UUID? = lastDamager[player.uuid]?.let {
        if (System.currentTimeMillis() - it.second < 5000) it.first else null
    }
}

fun GenericGame.enableDamageTracker(): DamageTracker = DamageTracker(this)
