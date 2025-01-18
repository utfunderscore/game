package org.readutf.arena.minestom.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import org.readutf.game.engine.utils.Position
import java.util.*
import java.util.function.Consumer

inline fun <reified T : Event> EventNode<Event>.addListener(noinline listener: (T) -> Unit): EventNode<Event> = addListener(T::class.java) {
    listener(it)
}

inline fun <reified T : Event> EventNode<Event>.addListener(listener: Consumer<T>) {
    addListener(T::class.java, listener)
}

fun UUID?.getPlayer(): Player? = this?.let { MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it) }

inline fun <T> List<T>.distinctBySimilar(isSimilar: (T, T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (result.none { isSimilar(it, item) }) {
            result.add(item)
        }
    }
    return result
}

fun Point.toPosition(): Position = Position(x(), y(), z())

fun <T> merge(vararg lists: List<T>): List<T> {
    val result = mutableListOf<T>()
    for (list in lists) {
        result += list
    }
    return result
}
