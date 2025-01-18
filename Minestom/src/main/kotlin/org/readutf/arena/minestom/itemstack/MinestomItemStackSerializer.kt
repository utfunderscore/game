package org.readutf.arena.minestom.itemstack

import com.github.michaelbull.result.Ok
import net.kyori.adventure.nbt.BinaryTagIO
import net.minestom.server.item.ItemStack
import org.readutf.arena.minestom.platform.MinestomItemStack
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MinestomItemStackSerializer : ItemStackSerializer<MinestomItemStack> {

    private val nbtWriter = BinaryTagIO.writer()
    private val nbtReader = BinaryTagIO.reader()

    override fun serialize(
        item: MinestomItemStack,
        outputStream: ByteArrayOutputStream,
    ): SResult<Unit> {
        val itemStream = ByteArrayOutputStream()

        val nbt = item.itemStack.toItemNBT()
        nbtWriter.write(nbt, itemStream)

        outputStream.writeInt(itemStream.size())
        outputStream.write(itemStream.toByteArray())
        return Ok(Unit)
    }

    override fun deserialize(inputStream: ByteArrayInputStream): SResult<MinestomItemStack> {
        val size = inputStream.readInt()
        val bytes = ByteArray(size)
        inputStream.read(bytes)

        return Ok(MinestomItemStack(ItemStack.fromItemNBT(nbtReader.read(ByteArrayInputStream(bytes)))))
    }
}
