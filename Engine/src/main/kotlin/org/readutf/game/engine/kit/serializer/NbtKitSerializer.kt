package org.readutf.game.engine.kit.serializer

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class NbtKitSerializer<T : ArenaItemStack<T>>(private val serializer: ItemStackSerializer<T>) : KitSerializer<T> {
    override fun serialize(
        kit: Kit<T>,
        outputStream: OutputStream,
    ): SResult<Unit> {
        val byteOutputStream = ByteArrayOutputStream()

        byteOutputStream.writeInt(kit.items.size)
        kit.items.forEach { itemStack ->
            serializer.serialize(itemStack, byteOutputStream)
        }

        outputStream.write(byteOutputStream.toByteArray())
        return Ok(Unit)
    }

    override fun deserialize(inputStream: InputStream): SResult<Kit<T>> {
        val inputByteStream: ByteArrayInputStream = inputStream.readAllBytes().inputStream()

        val itemsSize = inputByteStream.readInt()
        val items = ArrayList<T>(itemsSize)
        repeat(itemsSize) {
            items += serializer.deserialize(inputByteStream).getOrElse { return Err(it) }
        }

        return Ok(Kit(items))
    }

    override fun toString(): String = "NbtKitSerializer"
}
