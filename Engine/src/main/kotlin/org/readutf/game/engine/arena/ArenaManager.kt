package org.readutf.game.engine.arena

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.Schematic
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.store.schematic.ArenaSchematicStore
import org.readutf.game.engine.arena.store.template.ArenaTemplateStore
import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.location.PositionSettings
import org.readutf.game.engine.types.Result
import java.util.*
import kotlin.reflect.KClass

class ArenaManager(
    private val gameSettingsManager: GameSettingsManager,
    private val templateStore: ArenaTemplateStore,
    private val schematicStore: ArenaSchematicStore,
) {
    private val logger = KotlinLogging.logger {}

    fun createArena(
        arenaName: String,
        schematic: Schematic,
        vararg gameTypes: String,
    ): Result<ArenaTemplate> {
        val positions = extractMarkerPositions(schematic)

        for (gameType in gameTypes) {
            gameSettingsManager.validatePositionRequirements(gameType, positions).mapError { return it }
        }
        logger.info { "Created arena $arenaName with positions $positions" }

        val template = ArenaTemplate(arenaName, positions, Vec.fromPoint(schematic.size), listOf(*gameTypes))

        templateStore.save(template).mapError { return it }

        schematicStore.save(arenaName, schematic, template.positions.values.toList()).mapError { return it }

        return Result.success(template)
    }

    fun <T : PositionSettings> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): Result<Arena<T>> {
        val template: ArenaTemplate =
            templateStore.load(arenaName).mapError { return it }

        val schematicInstance = schematicStore.load(arenaName).mapError { return it }

        val positionSettings =
            gameSettingsManager.loadPositionSettings(template.positions, kClass).mapError { return it }

        return Result.success(
            Arena(
                arenaId = UUID.randomUUID(),
                instance = schematicInstance,
                positionSettings = positionSettings,
                positions = template.positions,
            ) { arena ->
                MinecraftServer.getInstanceManager().unregisterInstance(arena.instance)
            },
        )
    }

    private fun extractMarkerPositions(schematic: Schematic): Map<String, Marker> {
        val positions = mutableMapOf<String, Marker>()

        schematic.blockEntities.filter { it.value.id == "minecraft:sign" }.forEach { (key, signEntity) ->
            val markerLines = extractMarkerLines(signEntity.trimmedTag)

            if (!markerLines.getOrNull(0).equals("#marker", true)) {
                logger.debug { "Sign at $key does not start with #marker" }
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
                println("'${markerLines[2]}'")
                logger.info { "Marker at $key does has an offset" }
                offset = markerLines[2].split(",").mapNotNull { runCatching { it.toInt() }.getOrNull() }
            }
            if (offset.size != 3) {
                logger.info { "Marker at $key does not have a valid offset" }
                return@forEach
            }

            val originalPosition = Vec.fromPoint(signEntity.point)

            positions[markerName] =
                Marker(
                    Vec(
                        originalPosition.x + offset[0].toDouble(),
                        originalPosition.y + offset[1].toDouble(),
                        originalPosition.z + offset[2].toDouble(),
                    ),
                    originalPosition,
                    markerLines = arrayOf(markerLines[0], markerLines[1], markerLines[2], markerLines[3]),
                )
        }

        return positions
    }

    private fun extractMarkerLines(compoundBinaryTag: CompoundBinaryTag): List<String> =
        compoundBinaryTag
            .getCompound("front_text")
            .getList("messages")
            .map {
                (it as StringBinaryTag).value()
            }.map {
                it.substring(1, it.length - 1)
            }
}
