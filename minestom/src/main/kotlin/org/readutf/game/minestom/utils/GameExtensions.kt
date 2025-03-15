package org.readutf.game.minestom.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.world.GameWorld
import org.readutf.game.minestom.platform.MinestomWorld

fun GameWorld.getInstance(): Instance = (this as MinestomWorld).instance

fun GameTeam.getOnlinePlayers(): List<Player> = players.mapNotNull { MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it) }
