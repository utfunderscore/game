package org.readutf.game.engine.kit

import com.github.michaelbull.result.Result
import java.io.InputStream
import java.io.OutputStream

interface KitSerializer {
    fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): Result<Unit, Throwable>

    fun deserialize(inputStream: InputStream): Kit
}
