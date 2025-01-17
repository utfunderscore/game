package org.readutf.game.engine.kit.serializer

import com.github.michaelbull.result.Ok
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.utils.SResult
import org.readutf.game.engine.utils.compress
import org.readutf.game.engine.utils.decompress
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class CompressedSerializer<T : ArenaItemStack<T>>(
    val kitSerializer: KitSerializer<T>,
) : KitSerializer<T> {
    override fun serialize(
        kit: Kit<T>,
        outputStream: OutputStream,
    ): SResult<Unit> {
        val stream = ByteArrayOutputStream()
        kitSerializer.serialize(kit, stream)
        outputStream.write(compress(stream.toByteArray()))
        return Ok(Unit)
    }

    override fun deserialize(inputStream: InputStream): SResult<Kit<T>> {
        val compressed = inputStream.readAllBytes()
        val decompressed = decompress(compressed)
        return kitSerializer.deserialize(decompressed.inputStream())
    }

    override fun toString(): String = "Compressed($kitSerializer)"
}
