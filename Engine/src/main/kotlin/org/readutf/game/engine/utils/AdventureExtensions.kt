package org.readutf.game.engine.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

operator fun TextComponent.plus(heartLine: Component): Component = this.append(heartLine)

private val legacySerializer = LegacyComponentSerializer.legacy('&')

fun String.toComponent() = legacySerializer.deserialize(this)
