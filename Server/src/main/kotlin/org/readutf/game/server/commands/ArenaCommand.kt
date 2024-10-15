package org.readutf.game.server.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.SchematicReader
import org.readutf.game.engine.arena.ArenaManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.cli.ConsoleActor
import revxrsal.commands.command.CommandActor
import java.io.File
import java.io.FileInputStream
import kotlin.time.measureTimedValue

class ArenaCommand(
    workDir: File,
    private val arenaManager: ArenaManager,
) {
    @Transient private val logger = KotlinLogging.logger { }
    private val schematicFolder = File(workDir, "schematics")

    init {
        if (!schematicFolder.exists()) {
            schematicFolder.mkdirs()
        }
        if (!schematicFolder.isDirectory) {
            throw IllegalStateException("Schematics folder is not a directory")
        }
    }

    @Command("arena create <name> <schematic> <gametype>")
    fun create(
        actor: CommandActor,
        name: String,
        @Named("schematic") schematicName: String,
        @Named("gametype") gameType: String,
    ) {
        val schematicFile = File(schematicFolder, "$schematicName.schem")
        if (!schematicFile.exists()) {
            actor.reply("Schematic $schematicName does not exist")
            return
        }

        val schematic =
            FileInputStream(schematicFile).use { input ->
                SchematicReader().read(input)
            }

        val (template, time) =
            measureTimedValue {
                arenaManager.createArena(name, schematic, gameType).onFailure {
                    actor.error("Failed to create arena: ${it.getError()}")
                    return
                }
            }

        actor.reply("&aSaved template (points: ${template.positions.size}) in ${time.inWholeMilliseconds}ms")
    }

    @Command("arena listschems", "arena schems", "arena schematics")
    fun listSchematics(consoleActor: ConsoleActor) {
        val schematics =
            schematicFolder.list() ?: let {
                consoleActor.reply("No schematics found")
                return
            }

        for (schematic in schematics) {
            consoleActor.reply("&6&lSchematics:")
            consoleActor.reply("&7* &e$schematic")
        }
    }
}
