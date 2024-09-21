package org.readutf.game.engine.team

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

data class GameTeam(
    val players: List<UUID>,
) {
    constructor(vararg players: UUID) : this(listOf(*players))

    fun getOnlinePlayers(): List<Player> = players.mapNotNull { MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it) }
}
