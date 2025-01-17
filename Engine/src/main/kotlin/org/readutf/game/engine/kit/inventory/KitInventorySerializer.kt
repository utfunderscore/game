package org.readutf.game.engine.kit.inventory

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.serializer.PalletKitSerializer
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Serializes and deserializes the items in a players
 * inventory based on the kit they were given
 */
class KitInventorySerializer<T : ArenaItemStack<T>>(private val palletKitSerializer: PalletKitSerializer<T>) {
    fun serialize(
        kit: Kit<T>,
        kitInventory: KitInventory<T>,
    ): Result<OutputStream> {
        val stream = ByteArrayOutputStream()

        stream.writeInt(kitInventory.items.size)
        kitInventory.items.forEach {
            palletKitSerializer.writeItemStackByPalletIndex(stream, kit.pallet, it)
        }

        stream.writeInt(kitInventory.armorItems.size)
        kitInventory.armorItems.forEach {
            palletKitSerializer.writeItemStackByPalletIndex(stream, kit.pallet, it)
        }

        return Result.success(stream)
    }

    fun deserialize(
        kit: Kit<T>,
        bytes: ByteArray,
    ): SResult<KitInventory<T>> {
        val inputStream = ByteArrayInputStream(bytes)

        val itemsSize = inputStream.readInt()
        val items = ArrayList<T>(itemsSize)

        repeat(itemsSize) {
            items += palletKitSerializer.readItemStackByPalletIndex(inputStream, kit.pallet).getOrElse { return Err(it) }
        }

        val armorItemsSize = inputStream.readInt()
        val armorItems = ArrayList<T>(armorItemsSize)

        repeat(armorItemsSize) {
            armorItems += palletKitSerializer.readItemStackByPalletIndex(inputStream, kit.pallet).getOrElse { return Err(it) }
        }

        return Ok(KitInventory(items, armorItems))
    }
}
