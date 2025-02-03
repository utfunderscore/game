package org.readutf.game.engine.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.coordinate.BlockVec
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
    private val logger = KotlinLogging.logger {}

    fun contains(point: Point): Boolean {
        val blockVec = BlockVec(point)

        return blockVec.x() in minX..maxX && blockVec.y() in minY..maxY && blockVec.z() in minZ..maxZ
    }

    fun getBlocks(): Iterator<BlockVec> =
        sequence {
            for (x in minX.toInt()..maxX.toInt()) {
                for (y in minY.toInt()..maxY.toInt()) {
                    for (z in minZ.toInt()..maxZ.toInt()) {
                        yield(BlockVec(x, y, z))
                    }
                }
            }
        }.iterator()

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
