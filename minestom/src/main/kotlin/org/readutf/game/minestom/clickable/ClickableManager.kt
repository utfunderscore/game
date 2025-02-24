package org.readutf.game.minestom.clickable

import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.Feature
import java.util.UUID
import kotlin.reflect.KClass

class ClickableManager : Feature() {
    private val clickables = mutableMapOf<UUID, Clickable>()

    private val rightClickEvent = TypedGameListener<PlayerUseItemEvent> { event ->

        val player = event.player
        val item = event.itemStack
        useClickable(item, player)
    }
    private val leftClickEvent = TypedGameListener<PlayerHandAnimationEvent> { event ->
        val player = event.player
        val item = player.itemInMainHand
        useClickable(item, player)
    }

    override fun getListeners(): Map<KClass<*>, GameListener> = mapOf(
        PlayerUseItemEvent::class to rightClickEvent,
        PlayerHandAnimationEvent::class to leftClickEvent,
    )

    override fun shutdown() {
    }

    private fun useClickable(
        item: ItemStack,
        player: Player,
    ) {
        val tagValue: String? = item.getTag(IDENTIFIER_TAG)
        if (tagValue != null) {
            val id = UUID.fromString(tagValue)
            val clickable = clickables[id] ?: return
            clickable.onClick(player, true)

            if (clickable.refreshOnClick) {
                setItem(player, player.heldSlot.toInt(), clickable)
            }
        }
    }

    fun giveItem(
        player: Player,
        clickable: Clickable,
    ) {
        clickables[clickable.id] = clickable
        val itemstack = clickable.getItemStack().withTag(IDENTIFIER_TAG, clickable.id.toString())

        player.inventory.addItemStack(itemstack)
    }

    fun setItem(
        player: Player,
        slot: Int,
        clickable: Clickable,
    ) {
        clickables[clickable.id] = clickable
        val itemstack = clickable.getItemStack().withTag(IDENTIFIER_TAG, clickable.id.toString())

        player.inventory.setItemStack(slot, itemstack)
    }

    companion object {
        private val IDENTIFIER_TAG = Tag.String("clickable_identifier")
    }
}
