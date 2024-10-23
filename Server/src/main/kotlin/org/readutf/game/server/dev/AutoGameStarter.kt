package org.readutf.game.server.dev

import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerSpawnEvent
import org.readutf.game.engine.utils.addListener
import org.readutf.game.server.game.GameTypeManager

class AutoGameStarter(
    gameTypeManager: GameTypeManager,
) {
    init {

        val gamemode = gameTypeManager.getCreator("thebridge") ?: throw Exception("Gamemode not found")

        val game = gamemode.create().getOrThrow()

        game.start().getOrThrow()

        MinecraftServer.getGlobalEventHandler().addListener<PlayerSpawnEvent> { e ->

            MinecraftServer
                .getSchedulerManager()
                .scheduleNextTick {
                    if (e.isFirstSpawn) {
                        e.player.permissionLevel = 4

                        val team =
                            game.getTeams().minBy { team ->
                                team.players.size
                            }

                        game.addPlayer(e.player, team).debug { }.getOrThrow()
                    }
                }
        }
    }
}
