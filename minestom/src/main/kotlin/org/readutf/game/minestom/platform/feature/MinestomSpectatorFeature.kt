package org.readutf.game.minestom.platform.feature

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.respawning.RespawningFeature
import org.readutf.game.minestom.platform.feature.spectator.DamageListener
import org.readutf.game.minestom.platform.feature.spectator.SpectatorData
import org.readutf.game.minestom.platform.feature.spectator.SpectatorManager
import org.readutf.game.minestom.utils.getOnline
import org.readutf.game.minestom.utils.toPlayer
import org.readutf.game.minestom.utils.toPos
import java.util.UUID
import java.util.function.Predicate
import kotlin.reflect.KClass

class MinestomSpectatorFeature(
    game: GenericGame,
    respawnHandler: RespawningFeature,
) : SpectatorManager(
    game,
    respawnHandler,
) {

    val entityDamageListener = TypedGameListener<EntityDamageEvent> { e ->
        if (isSpectator(e.entity.uuid)) {
            e.isCancelled = true
        }

        val attacker = e.damage.attacker?.uuid
        if (attacker != null && isSpectator(attacker)) {
            e.isCancelled = true
        }
    }

    override fun registerDamageListener(damageListener: DamageListener): Pair<KClass<*>, GameListener> {
        return Pair(
            DamageListener::class,
            GameListener { event ->
                val event = event as? EntityDamageEvent ?: return@GameListener
                val player = event.entity as? Player ?: return@GameListener

                damageListener.onDamage(player.uuid, event.damage.amount, event.isCancelled)
            },
        )
    }

    override fun getListeners(): Map<KClass<*>, GameListener> = mapOf(
        Pair(DamageListener::class, entityDamageListener),
    )

    override fun getHealth(playerId: UUID): Float = playerId.toPlayer()?.health ?: 20.0f

    override fun setSpectatorState(
        playerId: UUID,
        spectatorData: SpectatorData,
    ) {
        val player = playerId.toPlayer() ?: return

        player.teleport(spectatorData.position.toPos())

        player.updateViewableRule(SpectatorViewableRule)

        player.gameMode = GameMode.ADVENTURE
        player.isAllowFlying = true
        player.isFlying = true
    }

    override fun setNormalState(playerId: UUID) {
        val player = playerId.toPlayer() ?: return

        player.gameMode = GameMode.SURVIVAL
        player.updateViewableRule { true }
        player.isAllowFlying = false
        player.isFlying = false
    }

    fun getAlive(): List<Player> = game.getOnline().filter { isAlive(it.uuid) }
    override fun shutdown() {
        game.getOnline().forEach {
            println("Updating viewable rule for ${it.username}")
            it.updateViewableRule()
        }
    }

    object SpectatorViewableRule : Predicate<Player> {
        override fun test(t: Player): Boolean {
            val game = GameManager.getGameByPlayer(t.uuid) ?: return true
            val spectatorFeature = game.getFeature<MinestomSpectatorFeature>() ?: return true

            return spectatorFeature.isSpectator(t.uuid)
        }
    }
}
