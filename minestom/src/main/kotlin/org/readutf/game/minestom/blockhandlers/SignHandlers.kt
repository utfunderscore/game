package org.readutf.game.minestom.blockhandlers

import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID

class SignHandler : BlockHandler {
    override fun getBlockEntityTags(): Collection<Tag<*>> = mutableSetOf(
        Tag.Byte("is_waxed"),
        Tag.NBT("front_text"),
        Tag.NBT("back_text"),
    )

    override fun getNamespaceId(): NamespaceID = NamespaceID.from(KEY)

    companion object {
        val KEY: NamespaceID = NamespaceID.from("minecraft:sign")
    }
}
