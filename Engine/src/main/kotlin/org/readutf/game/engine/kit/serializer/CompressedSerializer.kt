package org.readutf.game.engine.kit.serializer

import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitSerializer
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.compress
import org.readutf.game.engine.utils.decompress
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class CompressedSerializer(
    val kitSerializer: KitSerializer,
) : KitSerializer {
    override fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): Result<Unit> {
        val stream = ByteArrayOutputStream()
        kitSerializer.serialize(kit, stream)
        outputStream.write(compress(stream.toByteArray()))
        return Result.empty()
    }

    override fun deserialize(inputStream: InputStream): Kit {
        val compressed = inputStream.readAllBytes()
        val decompressed = decompress(compressed)
        return kitSerializer.deserialize(decompressed.inputStream())
    }

    override fun toString(): String = "Compressed($kitSerializer)"
}
