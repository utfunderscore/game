package org.readutf.game.engine.settings

import org.readutf.game.engine.settings.store.GameSettingsStore

class GameSettingsManager(
    val gameSettingsStore: GameSettingsStore,
) {
    inline fun <reified T : GameSettings> getGameSettings(gameType: String): T = gameSettingsStore.load(gameType, T::class.java)

    inline fun <reified T : GameSettings> setDefaultSettings(
        gameType: String,
        gameSettings: T,
    ) {
        gameSettingsStore.save(gameType, gameSettings)
    }
}
