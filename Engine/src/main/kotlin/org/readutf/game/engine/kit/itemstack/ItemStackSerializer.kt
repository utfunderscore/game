package org.readutf.game.engine.kit.itemstack

import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

interface ItemStackSerializer<T : ArenaItemStack<T>> {

    fun serialize(
        itemStack: T,
        outputStream: ByteArrayOutputStream,
    ): SResult<Unit>

    fun deserialize(inputStream: ByteArrayInputStream): SResult<T>
}
