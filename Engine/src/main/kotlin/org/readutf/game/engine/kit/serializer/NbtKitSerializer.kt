package org.readutf.game.engine.kit.serializer

import net.minestom.server.item.ItemStack
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object NbtKitSerializer : KitSerializer {
    override fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): Result<Unit> {
        val byteOutputStream = ByteArrayOutputStream()

        byteOutputStream.writeInt(kit.items.size)
        kit.items.forEach { itemStack ->
            ItemStackSerializer.serialize(itemStack, byteOutputStream)
        }

        outputStream.write(byteOutputStream.toByteArray())
        return Result.empty()
    }

    override fun deserialize(inputStream: InputStream): Kit {
        val inputByteStream: ByteArrayInputStream = inputStream.readAllBytes().inputStream()

        val itemsSize = inputByteStream.readInt()
        val items = ArrayList<ItemStack>(itemsSize)
        repeat(itemsSize) {
            items += ItemStackSerializer.deserialize(inputByteStream)
        }

        return Kit(items)
    }

    override fun toString(): String = "NbtKitSerializer"
}
