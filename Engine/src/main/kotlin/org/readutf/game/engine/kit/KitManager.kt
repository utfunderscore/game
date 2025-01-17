package org.readutf.game.engine.kit

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.kit.itemstack.ItemStackSerializer
import org.readutf.game.engine.kit.serializer.CompressedSerializer
import org.readutf.game.engine.kit.serializer.NbtKitSerializer
import org.readutf.game.engine.kit.serializer.PalletKitSerializer
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class KitManager<T : ArenaItemStack<T>>(
    baseDir: File,
    itemStackSerializer: ItemStackSerializer<T>,
    platform: Platform<T>,
) {
    private val logger = KotlinLogging.logger {}
    private val kitFolder = File(baseDir, "kits")

    init {
        kitFolder.mkdirs()
    }

    private val kitSerializers: Map<Byte, KitSerializer<T>> =
        mapOf(
            0.toByte() to NbtKitSerializer(itemStackSerializer),
            1.toByte() to PalletKitSerializer(platform, itemStackSerializer),
            2.toByte() to CompressedSerializer(NbtKitSerializer(itemStackSerializer)),
            3.toByte() to CompressedSerializer(PalletKitSerializer(platform, itemStackSerializer)),
        )

    private val kits = mutableMapOf<String, Kit<T>>()

    fun loadKit(name: String): SResult<Kit<T>> {
        val local = kits[name]
        if (local != null) return Ok(local)

        val kitFile = File(kitFolder, "$name.kit")
        FileInputStream(kitFile).use { inputStream ->
            val type = inputStream.read().toByte()
            val serializer = kitSerializers[type] ?: return Err("Unknown kit type: $type")
            val kit = serializer.deserialize(inputStream).getOrElse { return Err(it) }
            kits[name] = kit
            return Ok(kit)
        }
    }

    fun saveKit(
        name: String,
        kit: Kit<T>,
    ) {
        kitFolder.mkdirs()
        val kitFile = File(kitFolder, "$name.kit")
        if (kitFile.exists()) {
            logger.warn { "Kit $name already exists, overwriting" }
        } else {
            kitFile.createNewFile()
        }
        kitFile.outputStream().use { outputStream ->
            val (byte, serializer) = findMinSize(kit)
            logger.info { "Using serializer $serializer for kit $name" }

            outputStream.write(byte.toInt())
            serializer.serialize(kit, outputStream)
        }
    }

    fun findMinSize(kit: Kit<T>): Pair<Byte, KitSerializer<T>> = kitSerializers
        .minBy {
            val outputStream = ByteArrayOutputStream()
            it.value.serialize(kit, outputStream)
            outputStream.size()
        }.toPair()
}
