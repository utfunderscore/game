package org.readutf.game.minestom.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.buildformat.common.markers.Position
import org.readutf.game.engine.GenericGame
import java.util.UUID

fun Point.toPosition(): Position = Position(x(), y(), z())

fun Position.toPoint(): Point = Pos(x, y, z)

fun Position.toPos(): Pos = Pos(x, y, z)

fun pos(
    x: Int,
    y: Int,
    z: Int,
): Position = Position(x.toDouble(), y.toDouble(), z.toDouble())

fun UUID.toPlayer(): Player? = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(this)

val legacyComponentSerializer = LegacyComponentSerializer.legacyAmpersand()

fun String.toComponent(): Component = legacyComponentSerializer.deserialize(this)

private val miniMessage = MiniMessage.miniMessage()

fun String.fromMiniMessage(): Component = miniMessage.deserialize(this)

fun GenericGame.getOnline(): List<Player> = MinecraftServer.getConnectionManager().onlinePlayers.toList()
