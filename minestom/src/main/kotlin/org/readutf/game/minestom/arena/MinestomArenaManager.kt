package org.readutf.game.minestom.arena

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.SpongeSchematic
import net.minestom.server.MinecraftServer
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.settings.PositionSettingsManager
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.minestom.arena.marker.MarkerUtils
import org.readutf.game.minestom.arena.store.schematic.ArenaSchematicStore
import org.readutf.game.minestom.arena.store.template.ArenaTemplateStore
import org.readutf.game.minestom.platform.MinestomWorld
import org.readutf.game.minestom.utils.getInstance
import org.readutf.game.minestom.utils.toComponent
import org.readutf.game.minestom.utils.toPosition
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class MinestomArenaManager(
    private val positionSettingsManager: PositionSettingsManager,
    private val templateStore: ArenaTemplateStore,
    private val schematicStore: ArenaSchematicStore,
) : ArenaManager() {
    private val logger = KotlinLogging.logger {}

    fun createArena(
        arenaName: String,
        schematic: SpongeSchematic,
        vararg gameTypes: String,
    ): CompletableFuture<Result<ArenaTemplate, Throwable>> {
        val positions = MarkerUtils.extractMarkerPositions(schematic)

        for (gameType in gameTypes) {
            positionSettingsManager
                .validatePositionRequirements(
                    gameType,
                    positions,
                ).getOrElse { return CompletableFuture.completedFuture(Err(it)) }
        }
        logger.info { "Created arena $arenaName with positions $positions" }

        val template = ArenaTemplate(arenaName, positions, schematic.size().toPosition(), listOf(*gameTypes))

        templateStore.save(template).getOrElse { return CompletableFuture.completedFuture(Err(it)) }

        return schematicStore.save(arenaName, schematic, template.positions.values.toList()).thenApply { Ok(template) }
    }

    override fun freeArena(arena: Arena<*>) {
    }

    override fun getTemplates(gameType: String): List<ArenaTemplate> = templateStore.findByGameType(gameType)

    override fun <T : PositionData> loadArena(
        arenaName: String,
        kClass: KClass<T>,
    ): Result<Arena<T>, Throwable> {
        val template: ArenaTemplate =
            templateStore.load(arenaName).getOrElse { return Err(it) }

        val schematicInstance = schematicStore.load(arenaName).getOrElse { return Err(it) }

        val positionSettings =
            positionSettingsManager.loadPositionData(template.positions, kClass).getOrElse { return Err(it) }

        return Ok(
            Arena(
                arenaId = UUID.randomUUID(),
                instance = MinestomWorld(schematicInstance),
                templateName = template.name,
                positionSettings = positionSettings,
                size = template.size,
                positions = template.positions,
            ) { arena ->
                for (player in arena.instance.getInstance().players) {
                    player.kick("Game ended".toComponent())
                }
                MinecraftServer.getInstanceManager().unregisterInstance(arena.instance.getInstance())
            },
        )
    }
}
