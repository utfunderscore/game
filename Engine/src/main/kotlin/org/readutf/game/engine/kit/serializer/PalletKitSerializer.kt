package org.readutf.game.engine.kit.serializer

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.item.ItemStack
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.readShort
import org.readutf.game.engine.utils.writeInt
import org.readutf.game.engine.utils.writeShort
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Generates a 'pallet' of each type of item that is contained within the kit,
 * then uses the index of each item in the pallet to serialize the kit.
 */
object PalletKitSerializer : KitSerializer {
    private val logger = KotlinLogging.logger {}

    override fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): Result<Unit> {
        val byteOutputStream = ByteArrayOutputStream()

        logger.debug { "Original Pallet items: $kit.pallet" }
        writeItemStacks(byteOutputStream, kit.pallet)

        byteOutputStream.writeInt(kit.items.size)
        kit.items.forEach { itemStack ->
            writeItemStackByPalletIndex(byteOutputStream, kit.pallet, itemStack)
        }

        outputStream.write(byteOutputStream.toByteArray())

        return Result.empty()
    }

    override fun deserialize(inputStream: InputStream): Kit {
        val byteInputStream = ByteArrayInputStream(inputStream.readAllBytes())

        val pallet = readItemStacks(byteInputStream)
        logger.debug { "Deserialize pallet items $pallet" }

        val itemsSize = byteInputStream.readInt()
        val items = ArrayList<ItemStack>(itemsSize)

        repeat(itemsSize) {
            items += readItemStackByPalletIndex(byteInputStream, pallet).getOrThrow()
        }

        return Kit(items)
    }

    fun writeItemStackByPalletIndex(
        palletBytes: ByteArrayOutputStream,
        pallet: List<ItemStack>,
        itemStack: ItemStack,
    ): Result<Unit> {
        val index = pallet.indexOfFirst { it.isSimilar(itemStack) }
        if (index == -1) return Result.failure("ItemStack not found in pallet")

        if (itemStack.isAir) {
            palletBytes.writeInt(-1)
            palletBytes.writeShort(0)
            return Result.success(Unit)
        }

        palletBytes.writeInt(index)
        palletBytes.writeShort(itemStack.amount().toShort())

        return Result.success(Unit)
    }

    fun readItemStackByPalletIndex(
        input: ByteArrayInputStream,
        pallet: List<ItemStack>,
    ): Result<ItemStack> {
        val index = input.readInt()
        val amount = input.readShort()

        if (index == -1) {
            return Result.success(ItemStack.AIR)
        }

        if (index >= pallet.size) return Result.failure("Index out of bounds")

        return Result.success(copy(pallet[index].withAmount(amount.toInt())))
    }

    fun writeItemStacks(
        palletBytes: ByteArrayOutputStream,
        pallet: List<ItemStack>,
    ) {
        palletBytes.writeInt(pallet.size)

        pallet.forEach { itemStack ->
            ItemStackSerializer.serialize(itemStack, palletBytes)
        }
    }

    fun readItemStacks(input: ByteArrayInputStream): List<ItemStack> {
        val pallet = mutableListOf<ItemStack>()

        val palletSize = input.readInt()

        repeat(palletSize) {
            pallet += ItemStackSerializer.deserialize(input)
        }

        return pallet
    }

    fun copy(itemStack: ItemStack): ItemStack {
        val outputStream = ByteArrayOutputStream()
        ItemStackSerializer.serialize(itemStack, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        return ItemStackSerializer.deserialize(inputStream)
    }

    override fun toString(): String = "PalletKitSerializer"
}
