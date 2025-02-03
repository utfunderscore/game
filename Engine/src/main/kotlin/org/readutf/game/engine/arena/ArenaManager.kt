package org.readutf.game.engine.arena

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.arena.marker.MarkerScanner
import org.readutf.game.engine.arena.schematic.ArenaSchematicStore
import org.readutf.game.engine.arena.store.ArenaTemplateStore
import org.readutf.game.engine.platform.schematic.ArenaSchematic
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.settings.PositionSettingsManager
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.utils.SResult
import java.util.*
import kotlin.reflect.KClass

class ArenaManager(
    private val markerScanner: MarkerScanner,
    private val positionSettingsManager: PositionSettingsManager,
    private val templateStore: ArenaTemplateStore,
    private val schematicStore: ArenaSchematicStore,
    private val arenaCreator: ArenaCreator,
) {

    fun saveTemplate(
        arenaName: String,
        schematic: ArenaSchematic,
        vararg supportedGames: String,
    ): SResult<ArenaTemplate> {
        val markers = markerScanner.getMarkerPositions(schematic)

        for (game in supportedGames) {
            positionSettingsManager.validatePositionRequirements(game, markers).getOrElse { return Err(it) }
        }

        val template = ArenaTemplate(
            name = arenaName,
            positions = markers,
            size = schematic.getSize(),
            supportedGames = supportedGames.toList(),
        )

        templateStore.save(template).getOrElse { return Err(it) }

        return Ok(template)
    }

    fun <T : PositionData, WORLD : ArenaWorld> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): SResult<Arena<T, WORLD>> {
        val template: ArenaTemplate =
            templateStore.load(arenaName).getOrElse { return Err(it) }

        val schematicInstance = schematicStore.load(arenaName).getOrElse { return Err(it) }

        val positionSettings =
            positionSettingsManager.loadPositionData(template.positions, kClass).getOrElse { return Err(it) }

        return Ok(
            arenaCreator.create(
                arenaId = UUID.randomUUID(),
                positionSettings = positionSettings,
                positions = template.positions,
            ),
        )
    }
}
