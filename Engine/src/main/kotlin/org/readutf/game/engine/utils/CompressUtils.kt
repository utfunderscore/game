package org.readutf.game.engine.utils

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterOutputStream

fun compress(bytes: ByteArray): ByteArray {
    val outputStream = ByteArrayOutputStream()
    val deflater = Deflater()
    val deflaterOutputStream = DeflaterOutputStream(outputStream, deflater)
    deflaterOutputStream.write(bytes)
    deflaterOutputStream.close()
    return outputStream.toByteArray()
}

fun decompress(bytes: ByteArray): ByteArray {
    val outputStream = ByteArrayOutputStream()
    val inflater = Inflater()
    val inflaterOutputStream = InflaterOutputStream(outputStream, inflater)
    inflaterOutputStream.write(bytes)
    inflaterOutputStream.close()
    return outputStream.toByteArray()
}
