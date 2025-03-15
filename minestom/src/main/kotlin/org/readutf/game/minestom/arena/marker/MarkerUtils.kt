package org.readutf.game.minestom.arena.marker

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.SpongeSchematic
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.utils.Position
import org.readutf.game.minestom.utils.toPosition

object MarkerUtils {
    private val logger = KotlinLogging.logger { }

    fun extractMarkerPositions(schematic: SpongeSchematic): Map<String, Marker> {
        val positions = mutableMapOf<String, Marker>()

        schematic.blockEntities().filter { it.data.getString("id") == "minecraft:sign" }.forEach { entity ->
            val markerLines = extractMarkerLines(entity.data)
            val point = Vec.fromPoint(entity.position).sub(schematic.offset)
            val key = point.toString()

            if (!markerLines.getOrNull(0).equals("#marker", true)) {
                logger.debug { "Sign at does not start with #marker lines: $markerLines" }
                return@forEach
            }

            logger.info { "Found marker at $key" }

            val markerName = markerLines[1]
            if (markerName.equals("", false)) {
                logger.info { "Marker at $key does not have a name" }
                return@forEach
            }
            var offset: List<Int> = mutableListOf(0, 0, 0)
            if (markerLines[2].isNotEmpty()) {
                logger.info { "Marker at $key does has an offset" }
                offset = markerLines[2].split(",", "-", " ").mapNotNull { runCatching { it.toInt() }.getOrNull() }
            }
            if (offset.size != 3) {
                logger.info { "Marker at $key does not have a valid offset" }
                return@forEach
            }

            positions[markerName] =
                Marker(
                    Position(
                        point.x + offset[0].toDouble(),
                        point.y + offset[1].toDouble(),
                        point.z + offset[2].toDouble(),
                    ),
                    point.toPosition(),
                    markerLines = arrayOf(markerLines[0], markerLines[1], markerLines[2], markerLines[3]),
                )
        }

        return positions
    }

    fun isMarker(block: Block): Boolean {
        val nbt = block.nbt() ?: return false
        val isSign = nbt.getString("id") == "minecraft:sign"
        return isSign && extractMarkerLines(nbt).getOrNull(0).equals("#marker", true)
    }

    private fun extractMarkerLines(compoundBinaryTag: CompoundBinaryTag): List<String> = compoundBinaryTag
        .getCompound("front_text")
        .getList("messages")
        .map {
            (it as StringBinaryTag).value()
        }.map {
            it.substring(1, it.length - 1)
        }
}
