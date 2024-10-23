package org.readutf.game.server.game.impl.settings

import org.readutf.game.server.game.dual.stages.AwaitingPlayersSettings
import org.readutf.game.engine.features.combat.KillMessageData

data class TheBridgeSettings(
    val awaitingPlayersSettings: AwaitingPlayersSettings = AwaitingPlayersSettings(),
    val numberOfLives: Int = 3,
    val killMessagesData: Map<String, KillMessageData> =
        mapOf(
            "minecraft:fall" to
                KillMessageData(
                    "&e{player} &f fell from a high place fighting &e{attacker}",
                    "&e{player} &f fell from a high place",
                ),
            "minecraft:out_of_world" to
                KillMessageData(
                    "&e{player} &f was knocked into the void by &e{attacker}",
                    "&e{player} &f fell into the void",
                ),
            "minecraft:on_fire" to
                KillMessageData(
                    "&e{player} &f was burnt to a crisp by &e{attacker}",
                    "&e{player} &f was burnt to a crisp",
                ),
            "minecraft:lava" to
                KillMessageData(
                    "&e{player} &f was melted by &e{attacker}",
                    "&e{player} &f was melted",
                ),
            "minecraft:drown" to
                KillMessageData(
                    "&e{player} &f drowned fighting &e{attacker}",
                    "&e{player} &f drowned",
                ),
            "default" to
                KillMessageData(
                    "&e{player} &f was killed by &e{attacker}",
                    "&e{player} &f was killed",
                ),
        ),
)
