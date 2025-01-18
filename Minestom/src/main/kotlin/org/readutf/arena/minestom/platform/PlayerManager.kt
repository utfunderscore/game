package org.readutf.arena.minestom.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import org.readutf.game.engine.platform.player.ArenaPlayer
import java.util.UUID

object PlayerManager {

    private val players = mutableMapOf<UUID, MinestomPlayer>()

    init {
        MinecraftServer.getGlobalEventHandler().addListener<PlayerSpawnEvent> {
            if (it.isFirstSpawn) {
                players[it.player.uuid] = MinestomPlayer(it.player)
            }
        }

        MinecraftServer.getGlobalEventHandler().addListener<PlayerDisconnectEvent> {
            players.remove(it.player.uuid)
        }
    }

    fun getPlayer(uuid: UUID): MinestomPlayer? = players[uuid]

    fun getPlayer(player: Player): MinestomPlayer = players.getOrPut(player.uuid) { MinestomPlayer(player) }
}

fun Player.toArenaPlayer() = PlayerManager.getPlayer(this)

fun ArenaPlayer<*>.toPlayer(): Player = (this as MinestomPlayer).player
