package org.readutf.game.engine.kit

import org.readutf.game.engine.utils.SResult
import java.io.InputStream
import java.io.OutputStream

interface KitSerializer {
    fun serialize(
        kit: Kit,
        outputStream: OutputStream,
    ): SResult<Unit>

    fun deserialize(inputStream: InputStream): Kit
}
