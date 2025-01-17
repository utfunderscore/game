package org.readutf.game.engine.kit

import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import java.io.InputStream
import java.io.OutputStream

interface KitSerializer<T : ArenaItemStack<T>> {
    fun serialize(
        kit: Kit<T>,
        outputStream: OutputStream,
    ): SResult<Unit>

    fun deserialize(inputStream: InputStream): SResult<Kit<T>>
}
