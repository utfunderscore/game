package org.readutf.game.engine.kit

import org.readutf.game.engine.types.Result
import java.io.InputStream
import java.io.OutputStream

interface KitSerializer {
    fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): Result<Unit>

    fun deserialize(inputStream: InputStream): Kit
}
