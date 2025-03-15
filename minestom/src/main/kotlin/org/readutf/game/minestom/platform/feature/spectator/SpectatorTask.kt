package org.readutf.game.minestom.platform.feature.spectator

import org.readutf.game.engine.schedular.RepeatingGameTask

class SpectatorTask(
    private val spectatorManager: SpectatorManager,
) : RepeatingGameTask(0, 50) {
    override fun run() {
        for (onlineSpectator in spectatorManager.getOnlineSpectators()) {
            val spectatorData = spectatorManager.getSpectatorData(onlineSpectator) ?: continue
            if (!spectatorData.respawn) return

            if (spectatorData.respawnTime.isBefore(java.time.LocalDateTime.now())) {
                spectatorManager.respawnSpectator(spectatorData)
            }
        }
    }
}
