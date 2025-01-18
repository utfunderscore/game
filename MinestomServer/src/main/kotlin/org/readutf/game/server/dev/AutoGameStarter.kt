package org.readutf.game.server.dev

import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.getOrThrow
import net.bladehunt.kotstom.dsl.listen
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerSpawnEvent
import org.readutf.arena.minestom.platform.toArenaPlayer
import org.readutf.game.server.game.GameTypeManager

class AutoGameStarter(
    gameTypeManager: GameTypeManager,
) {
    init {

        val gamemode = gameTypeManager.getCreator("thebridge") ?: throw Exception("Gamemode not found")

        val game = gamemode.create().getOrThrow { error("Could not create gamemode") }

        game.start().getOrThrow { error("Could not start game") }

        MinecraftServer.getGlobalEventHandler().listen<PlayerSpawnEvent> { e ->

            MinecraftServer
                .getSchedulerManager()
                .scheduleNextTick {
                    if (e.isFirstSpawn) {
                        e.player.permissionLevel = 4

                        val team =
                            game.getTeams().minBy { team ->
                                team.players.size
                            }

                        game.addPlayer(e.player.toArenaPlayer(), team).getOrElse { error("Could not add player to game") }
                    }
                }
        }
    }
}
