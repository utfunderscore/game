package org.readutf.arena.minestom.itemstack

import com.github.michaelbull.result.Ok
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MinestomItemStackSerializer : ItemStackSerializer {

    fun serialize(
        itemStack: ArenaItemStack,
        outputStream: ByteArrayOutputStream,
    ): SResult<Unit> {
        val itemStream = ByteArrayOutputStream()

        val nbt = itemStack.toItemNBT()
        nbtWriter.write(nbt, itemStream)

        outputStream.writeInt(itemStream.size())
        outputStream.write(itemStream.toByteArray())
        Ok(Unit)
    }

    fun deserialize(inputStream: ByteArrayInputStream): SResult<ArenaItemStack> {
        val size = inputStream.readInt()
        val bytes = ByteArray(size)
        inputStream.read(bytes)

        return ItemStack.fromItemNBT(nbtReader.read(ByteArrayInputStream(bytes)))
    }
}
