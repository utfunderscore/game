package org.readutf.game.minestom.clickable.devtools

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.readutf.game.minestom.clickable.Clickable

abstract class RollingDevTool<T>(
    private val values: List<T>,
) : Clickable() {
    private var currentIndex = 0

    abstract fun onUpdate(
        player: Player,
        value: T,
        clickType: Boolean,
    )

    abstract fun getMaterial(value: T): Material

    abstract fun getTitle(value: T): Component

    override fun getItemStack(): ItemStack {
        val value = values[currentIndex]
        return ItemStack
            .of(getMaterial(value))
            .withCustomName(getTitle(value))
            .withLore(
                Component.text("Current value: $value"),
                Component.text("Right click to increase, left click to decrease"),
            )
    }

    override fun onClick(
        player: Player,
        clickType: Boolean,
    ) {
        if (currentIndex >= values.size - 1) {
            currentIndex = 0
        } else {
            currentIndex += if (clickType) 1 else -1
        }
    }
}

fun rollingDevTool(
    values: List<Int>,
    builder: RollingDevToolBuilder<Int>.() -> Unit,
): RollingDevTool<Int> {
    val rollingDevToolBuilder = RollingDevToolBuilder<Int>().apply(builder)
    return object : RollingDevTool<Int>(values) {
        override fun onUpdate(
            player: Player,
            value: Int,
            clickType: Boolean,
        ) {
            rollingDevToolBuilder.onUpdate(player, value, clickType)
        }

        override fun getMaterial(value: Int): Material = rollingDevToolBuilder.materialProvider(value)

        override fun getTitle(value: Int): Component = rollingDevToolBuilder.titleProvider(value)
    }
}

class RollingDevToolBuilder<T> {
    var values = emptyList<T>()

    var materialProvider: (T) -> Material = { Material.STONE }

    var titleProvider: (T) -> Component = { Component.text("Rolling DevTool") }

    var onUpdate: (Player, T, Boolean) -> Unit = { _, _, _ -> }
}
