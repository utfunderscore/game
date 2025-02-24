package org.readutf.game.engine.utils

import io.github.oshai.kotlinlogging.KotlinLogging

class Cuboid(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
) {
    private val logger = KotlinLogging.logger {}

    fun contains(point: Position): Boolean = point.x in minX..maxX && point.y in minY..maxY && point.z in minZ..maxZ

    fun getBlocks(): Iterator<Position> = sequence {
        for (x in minX.toInt()..maxX.toInt()) {
            for (y in minY.toInt()..maxY.toInt()) {
                for (z in minZ.toInt()..maxZ.toInt()) {
                    yield(Position(x.toDouble(), y.toDouble(), z.toDouble()))
                }
            }
        }
    }.iterator()

    fun getSize(): Position = Position(maxX - minX, maxY - minY, maxZ - minZ)

    fun getMin(): Position = Position(minX, minY, minZ)

    fun getMax(): Position = Position(maxX, maxY, maxZ)

    companion object {
        fun fromPositions(
            vec1: Position,
            vec2: Position,
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
