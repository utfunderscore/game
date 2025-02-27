package org.readutf.game.minestom.platform.feature.spectator

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.game.features.respawning.RespawningFeature
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.minestom.platform.feature.spectator.event.GameSpectateEvent
import java.util.UUID
import kotlin.reflect.KClass

abstract class SpectatorManager(
    val game: GenericGame,
    val respawnHandler: RespawningFeature,
) : Feature() {
    private val spectators = mutableMapOf<UUID, SpectatorData>()
    private val externalSpectators = mutableListOf<UUID>()

    override fun getListeners(): Map<KClass<*>, GameListener> = mapOf(registerDamageListener(SpectatorListener(game = game, spectatorManager = this)))

    override fun getTasks(): List<GameTask> = listOf(SpectatorTask(this))

    fun setSpectator(spectatorData: SpectatorData) {
        spectators[spectatorData.playerId] = spectatorData

        if (spectatorData.external) {
            externalSpectators.add(spectatorData.playerId)
            return
        }
        game.callEvent(GameSpectateEvent(game, spectatorData))

        spectators[spectatorData.playerId] = spectatorData

        setSpectatorState(spectatorData.playerId, spectatorData)
    }

    fun respawnSpectator(spectatorData: SpectatorData) {
        if (externalSpectators.contains(spectatorData.playerId)) return
        val data = spectators[spectatorData.playerId] ?: return

        spectators.remove(spectatorData.playerId)

        if (respawnHandler.respawn(data.playerId).isErr) {
            spectators[data.playerId] = data
            return
        }

        setNormalState(data.playerId)
    }

    abstract fun getHealth(playerId: UUID): Float

    /**
     * Requirements:
     *  * Teleport player to the spectator position
     *  * Update viewable rule to only show other spectators
     *  * Set the player's gamemode to adventure
     *  * Allow flying
     *  * Set flying to true
     */
    abstract fun setSpectatorState(
        playerId: UUID,
        spectatorData: SpectatorData,
    )

    /**
     * Requirements:
     * * Update viewable rule to show player to all other players
     * * Set the player's gamemode to survival
     * * Disallow flying
     * * Set flying to false
     *
     */
    abstract fun setNormalState(playerId: UUID)

    abstract fun registerDamageListener(damageListener: DamageListener): Pair<KClass<*>, GameListener>

    fun getOnlineSpectators(): List<UUID> = spectators.keys.filter { game.getOnlinePlayers().contains(it) }.toList()

    fun isAlive(playerId: UUID): Boolean = !spectators.containsKey(playerId)

    fun isSpectator(playerId: UUID): Boolean = spectators.containsKey(playerId)

    fun getAlivePlayers(): List<UUID> = game.getOnlinePlayers().filter { !spectators.containsKey(it) }

    fun getSpectators(): List<SpectatorData> = spectators.values.toList()

    fun getSpectatorData(playerId: UUID): SpectatorData? = spectators[playerId]
}
