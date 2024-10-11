package org.readutf.game.engine.utils

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec

class Cuboid(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
) {
    fun contains(point: Point) = point.x() in minX..maxX && point.y() >= minY && point.y() <= maxY && point.z() >= minZ && point.z() <= maxZ

    companion object {
        fun fromVecs(
            vec1: Vec,
            vec2: Vec,
        ): Cuboid {
            val minX = vec1.x.coerceAtMost(vec2.x)
            val minY = vec1.y.coerceAtMost(vec2.y)
            val minZ = vec1.z.coerceAtMost(vec2.z)
            val maxX = vec1.x.coerceAtLeast(vec2.x)
            val maxY = vec1.y.coerceAtLeast(vec2.y)
            val maxZ = vec1.z.coerceAtLeast(vec2.z)
            return Cuboid(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }
}
