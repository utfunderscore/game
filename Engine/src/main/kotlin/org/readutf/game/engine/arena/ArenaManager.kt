package org.readutf.game.engine.arena

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.Schematic
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import org.readutf.game.engine.arena.store.ArenaTemplateStore
import org.readutf.game.engine.game.settings.GameSettingsManager
import org.readutf.game.engine.types.Position
import org.readutf.game.engine.types.Result

class ArenaManager(
    private val gameSettingsManager: GameSettingsManager,
    private val templateStore: ArenaTemplateStore,
) {
    private val logger = KotlinLogging.logger {}

    fun createArena(
        arenaName: String,
        schematic: Schematic,
        vararg gameTypes: String,
    ): Result<ArenaTemplate> {
        val positions = mutableMapOf<String, Position>()

        schematic.blockEntities.filter { it.value.id == "minecraft:sign" }.forEach { (key, signEntity) ->
            val markerLines = extractMarkerLines(signEntity.trimmedTag)

            if (!markerLines.getOrNull(0).equals("#marker", true)) {
                logger.info { "Sign at $key does not start with #marker" }
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

            val originalPosition = Position.parse(signEntity.point)

            positions[markerName] =
                Position(
                    originalPosition.x + offset[0],
                    originalPosition.y + offset[1],
                    originalPosition.z + offset[2],
                )
        }

        for (gameType in gameTypes) {
            gameSettingsManager.validatePositionRequirements(gameType, positions).onFailure { failureReason ->
                return Result.failure(failureReason)
            }
        }
        logger.info { "Created arena $arenaName with positions $positions" }

        val template = ArenaTemplate(arenaName, positions, Position.parse(schematic.size), listOf(*gameTypes))

        val templateSaveResult = templateStore.saveArenaTemplate(template)
        if (templateSaveResult.isFailure) return Result.failure(templateSaveResult.getError())
        return Result.success(template)
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
