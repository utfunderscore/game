package org.readutf.game.engine.kit.serializer

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Generates a 'pallet' of each type of item that is contained within the kit,
 * then uses the index of each item in the pallet to serialize the kit.
 */
class PalletKitSerializer<T : ArenaItemStack<T>>(
    val platform: Platform<T>,
    private val serializer: ItemStackSerializer<T>,
) : KitSerializer<T> {
    private val logger = KotlinLogging.logger {}

    override fun serialize(
        kit: Kit<T>,
        outputStream: OutputStream,
    ): SResult<Unit> {
        val byteOutputStream = ByteArrayOutputStream()

        logger.debug { "Original Pallet items: $kit.pallet" }
        writeItemStacks(byteOutputStream, kit.pallet)

        byteOutputStream.writeInt(kit.items.size)
        kit.items.forEach { itemStack ->
            writeItemStackByPalletIndex(byteOutputStream, kit.pallet, itemStack)
        }

        outputStream.write(byteOutputStream.toByteArray())

        return Ok(Unit)
    }

    override fun deserialize(inputStream: InputStream): SResult<Kit<T>> {
        val byteInputStream = ByteArrayInputStream(inputStream.readAllBytes())

        val pallet = readItemStacks(byteInputStream).getOrElse { return Err(it) }
        logger.debug { "Deserialize pallet items $pallet" }

        val itemsSize = byteInputStream.readInt()
        val items = ArrayList<T>(itemsSize)

        repeat(itemsSize) {
            items.add(readItemStackByPalletIndex(byteInputStream, pallet).getOrElse { return Err(it) })
        }

        return Ok(Kit(items))
    }

    fun writeItemStackByPalletIndex(
        palletBytes: ByteArrayOutputStream,
        pallet: List<T>,
        itemStack: T,
    ): SResult<Unit> {
        val index = pallet.indexOfFirst { it.isSimilar(itemStack) }
        if (index == -1) return Err("ItemStack not found in pallet")

        if (itemStack.isAir()) {
            palletBytes.writeInt(-1)
            palletBytes.writeShort(0)
            return Ok(Unit)
        }

        palletBytes.writeInt(index)
        palletBytes.writeShort(itemStack.getAmount().toShort())

        return Ok(Unit)
    }

    fun readItemStackByPalletIndex(
        input: ByteArrayInputStream,
        pallet: List<T>,
    ): SResult<T> {
        val index = input.readInt()
        val amount = input.readShort()

        if (index == -1) Ok(platform.getAir())
        if (index >= pallet.size) return Err("Index out of bounds")

        return copy(pallet[index].withAmount(amount.toInt()))
    }

    private fun writeItemStacks(
        palletBytes: ByteArrayOutputStream,
        pallet: List<T>,
    ) {
        palletBytes.writeInt(pallet.size)

        pallet.forEach { itemStack ->
            serializer.serialize(itemStack, palletBytes)
        }
    }

    private fun readItemStacks(input: ByteArrayInputStream): SResult<List<T>> {
        val pallet = mutableListOf<T>()

        val palletSize = input.readInt()

        repeat(palletSize) {
            pallet.add(serializer.deserialize(input).getOrElse { return Err(it) })
        }

        return Ok(pallet)
    }

    private fun copy(itemStack: T): SResult<T> {
        val outputStream = ByteArrayOutputStream()
        serializer.serialize(itemStack, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        return serializer.deserialize(inputStream)
    }
}
