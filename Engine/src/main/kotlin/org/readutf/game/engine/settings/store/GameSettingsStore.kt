package org.readutf.game.engine.settings.store

interface GameSettingsStore {
    fun <T> save(
        gameType: String,
        any: T,
    )

    fun <T> load(
        gameType: String,
        clazz: Class<T>,
    ): T
}
