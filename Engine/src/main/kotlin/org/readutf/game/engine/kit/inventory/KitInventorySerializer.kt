package org.readutf.game.engine.kit.inventory

import net.minestom.server.item.ItemStack
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.serializer.PalletKitSerializer
import org.readutf.game.engine.utils.readInt
import org.readutf.game.engine.utils.writeInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Serializes and deserializes the items in a players
 * inventory based on the kit they were given
 */
class KitInventorySerializer {
    fun serialize(
        kit: Kit,
        kitInventory: KitInventory,
    ): Result<OutputStream> {
        val stream = ByteArrayOutputStream()

        stream.writeInt(kitInventory.items.size)
        kitInventory.items.forEach {
            PalletKitSerializer.writeItemStackByPalletIndex(stream, kit.pallet, it)
        }

        stream.writeInt(kitInventory.armorItems.size)
        kitInventory.armorItems.forEach {
            PalletKitSerializer.writeItemStackByPalletIndex(stream, kit.pallet, it)
        }

        return Result.success(stream)
    }

    fun deserialize(
        kit: Kit,
        bytes: ByteArray,
    ): KitInventory {
        val inputStream = ByteArrayInputStream(bytes)

        val itemsSize = inputStream.readInt()
        val items = ArrayList<ItemStack>(itemsSize)

        repeat(itemsSize) {
            items += PalletKitSerializer.readItemStackByPalletIndex(inputStream, kit.pallet).getOrThrow()
        }

        val armorItemsSize = inputStream.readInt()
        val armorItems = ArrayList<ItemStack>(armorItemsSize)

        repeat(armorItemsSize) {
            armorItems += PalletKitSerializer.readItemStackByPalletIndex(inputStream, kit.pallet).getOrThrow()
        }

        return KitInventory(items, armorItems)
    }
}
