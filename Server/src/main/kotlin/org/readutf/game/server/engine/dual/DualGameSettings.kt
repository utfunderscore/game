package org.readutf.game.server.engine.dual

import org.readutf.game.engine.settings.GameSettings

class DualGameSettings(
    val maxPlayers: Int = 2,
    val minStartPlayers: Int = 2,
    val playersReachedCountdown: Int = 15,
    val gameStartingMessage: String = "&7Game starting in &9{time} seconds",
    val awaitingPlayers: String = "&7Waiting for &9{players} &7more players...",
) : GameSettings()
