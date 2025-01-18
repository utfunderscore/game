package org.readutf.arena.minestom.platform.schematic

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import org.readutf.arena.minestom.platform.toPosition
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.marker.MarkerScanner
import org.readutf.game.engine.platform.schematic.ArenaSchematic
import org.readutf.game.engine.utils.Position

class SchematicMarkerScanner : MarkerScanner {

    private val logger = KotlinLogging.logger { }

    override fun getMarkerPositions(arenaSchematic: ArenaSchematic): Map<String, Marker> {
        val positions = mutableMapOf<String, Marker>()

        if (arenaSchematic !is MinestomSchematic) return positions
        val schematic = arenaSchematic.schematic

        schematic.blockEntities().filter { it.id == "minecraft:sign" }.forEach { sign ->
            val markerLines = extractMarkerLines(sign.data)
            val position = sign.position.toPosition()

            if (!markerLines.getOrNull(0).equals("#marker", true)) {
                logger.debug { "Sign at $position does not start with #marker lines: $markerLines" }
                return@forEach
            }

            logger.info { "Found marker at $position" }

            val markerName = markerLines[1]
            if (markerName.equals("", false)) {
                logger.info { "Marker at $position does not have a name" }
                return@forEach
            }
            var offset: List<Int> = mutableListOf(0, 0, 0)
            if (markerLines[2].isNotEmpty()) {
                logger.info { "Marker at $position does has an offset" }
                offset = markerLines[2].split(",", "-", " ").mapNotNull { runCatching { it.toInt() }.getOrNull() }
            }
            if (offset.size != 3) {
                logger.info { "Marker at $position does not have a valid offset" }
                return@forEach
            }

            positions[markerName] =
                Marker(
                    Position(
                        position.x + offset[0].toDouble(),
                        position.y + offset[1].toDouble(),
                        position.z + offset[2].toDouble(),
                    ),
                    position,
                    markerLines = arrayOf(markerLines[0], markerLines[1], markerLines[2], markerLines[3]),
                )
        }

        return positions
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
