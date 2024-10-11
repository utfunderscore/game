package org.readutf.game.engine.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ByteArrayOutputStream.writeInt(value: Int) {
    write(value shr 24)
    write(value shr 16)
    write(value shr 8)
    write(value)
}

fun ByteArrayInputStream.readInt(): Int = read() shl 24 or (read() shl 16) or (read() shl 8) or read()

fun ByteArrayOutputStream.writeShort(value: Short) {
    write(value.toInt() shr 8)
    write(value.toInt())
}

fun ByteArrayInputStream.readShort(): Short = (read() shl 8 or read()).toShort()

fun ByteArrayOutputStream.writeLong(value: Long) {
    write((value shr 56).toInt())
    write((value shr 48).toInt())
    write((value shr 40).toInt())
    write((value shr 32).toInt())
    write((value shr 24).toInt())
    write((value shr 16).toInt())
    write((value shr 8).toInt())
    write(value.toInt())
}

fun ByteArrayInputStream.readLong(): Long =
    (
        read().toLong() shl 56
            or (read().toLong() shl 48)
            or (read().toLong() shl 40)
            or (read().toLong() shl 32)
            or (read().toLong() shl 24)
            or (read().toLong() shl 16)
            or (read().toLong() shl 8)
            or read().toLong()
    )

fun ByteArrayOutputStream.writeVarInt(value: Int) {
    var value = value
    while (true) {
        if ((value and 0xFFFFFF80.toInt()) == 0) {
            write(value)
            return
        }
        write(value and 0x7F or 0x80)
        value = value ushr 7
    }
}

fun ByteArrayInputStream.readVarInt(): Int {
    var value = 0
    var size = 0
    var b: Int
    while (true) {
        b = read()
        value = value or (b and 0x7F shl size)
        size += 7
        if (size > 35) throw IllegalArgumentException("VarInt too big")
        if (b and 0x80 == 0) return value
    }
}
