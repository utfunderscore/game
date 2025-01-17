package org.readutf.game.engine.utils

class BlockPosition(x: Int, y: Int, z: Int) : Position(x = x, y = y, z = z) {
    constructor(x: Double, y: Double, z: Double) : this(x.toInt(), y.toInt(), z.toInt())

    constructor(position: Position) : this(position.x, position.y, position.z)

    fun getBlockX(): Int = x.toInt()

    fun getBlockY(): Int = y.toInt()

    fun getBlockZ(): Int = z.toInt()
}
