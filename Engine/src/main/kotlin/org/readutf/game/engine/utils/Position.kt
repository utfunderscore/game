package org.readutf.game.engine.utils

open class Position(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    fun add(x: Double, y: Double, z: Double): Position = Position(this.x + x, this.y + y, this.z + z)

    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())
}
