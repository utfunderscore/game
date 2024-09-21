package org.readutf.game.engine.types

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec

data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())

    fun toVec() = Vec(x, y, z)

    companion object {
        fun parse(point: Point) = Position(point.x(), point.y(), point.z())
    }
}
