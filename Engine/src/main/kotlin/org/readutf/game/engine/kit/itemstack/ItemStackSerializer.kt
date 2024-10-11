package org.readutf.game.engine.kit.itemstack

import net.kyori.adventure.nbt.BinaryTagIO
import net.minestom.server.item.ItemStack
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ItemStackSerializer {
    private val nbtWriter = BinaryTagIO.writer()
    private val nbtReader = BinaryTagIO.reader()

    fun serialize(
        itemStack: ItemStack,
        outputStream: ByteArrayOutputStream,
    ) {
        val itemStream = ByteArrayOutputStream()

        val nbt = itemStack.toItemNBT()
        nbtWriter.write(nbt, itemStream)

        outputStream.writeInt(itemStream.size())
        outputStream.write(itemStream.toByteArray())
    }

    fun deserialize(inputStream: ByteArrayInputStream): ItemStack {
        val size = inputStream.readInt()
        val bytes = ByteArray(size)
        inputStream.read(bytes)

        return ItemStack.fromItemNBT(nbtReader.read(ByteArrayInputStream(bytes)))
    }
}
