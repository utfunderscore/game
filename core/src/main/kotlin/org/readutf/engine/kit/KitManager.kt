package org.readutf.game.engine.kit

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.utils.SResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class KitManager(
    baseDir: File,
) {
    private val logger = KotlinLogging.logger {}
    private val kitFolder = File(baseDir, "kits").also { it.mkdirs() }
    private val kitSerializers: MutableMap<Byte, KitSerializer> = mutableMapOf()
    private val kits = mutableMapOf<String, Kit>()

    fun loadKit(name: String): SResult<Kit> {
        val local = kits[name]
        if (local != null) return Ok(local)

        val kitFile = File(kitFolder, "$name.kit")
        FileInputStream(kitFile).use { inputStream ->
            val type = inputStream.read().toByte()
            val serializer = kitSerializers[type] ?: let {
                logger.error { "Unknown kit type: $type" }
                return Err("Unknown kit type: $type")
            }
            val kit = serializer.deserialize(inputStream)
            kits[name] = kit
            return Ok(kit)
        }
    }

    fun saveKit(
        name: String,
        kit: Kit,
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

    fun registerSerializer(
        type: Byte,
        serializer: KitSerializer,
    ) {
        if (kitSerializers.containsKey(type)) {
            logger.warn { "Serializer for type $type already exists, overwriting" }
        }
        kitSerializers[type] = serializer
    }

    fun unregisterSerializer(type: Byte) {
        kitSerializers.remove(type)
    }

    private fun findMinSize(kit: Kit): Pair<Byte, KitSerializer> = kitSerializers
        .minBy {
            val outputStream = ByteArrayOutputStream()
            it.value.serialize(kit, outputStream)
            outputStream.size()
        }.toPair()
}
