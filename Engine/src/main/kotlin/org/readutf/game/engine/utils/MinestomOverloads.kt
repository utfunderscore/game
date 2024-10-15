package org.readutf.game.engine.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.util.function.Consumer

inline fun <reified T : Event> EventNode<Event>.addListener(noinline listener: (T) -> Unit): EventNode<Event> =
    addListener(T::class.java) {
        listener(it)
    }

inline fun <reified T : Event> EventNode<Event>.addListener(listener: Consumer<T>) {
    addListener(T::class.java, listener)
}

private val legacySerializer = LegacyComponentSerializer.legacy('&')

fun String.toComponent() = legacySerializer.deserialize(this)

inline fun <T> List<T>.distinctBySimilar(isSimilar: (T, T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (result.none { isSimilar(it, item) }) {
            result.add(item)
        }
    }
    return result
}

fun <T> merge(vararg lists: List<T>): List<T> {
    val result = mutableListOf<T>()
    for (list in lists) {
        result += list
    }
    return result
}

operator fun TextComponent.plus(heartLine: Component): Component = this.append(heartLine)
