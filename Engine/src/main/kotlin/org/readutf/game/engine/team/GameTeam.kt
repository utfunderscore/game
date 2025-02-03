package org.readutf.game.engine.team

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

open class GameTeam(
    val teamName: String,
    val players: MutableList<UUID>,
) {
    fun getOnlinePlayers(): List<Player> = players.mapNotNull { MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it) }
}
