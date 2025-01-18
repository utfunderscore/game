package org.readutf.game.server.commands

import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.reader.SpongeSchematicReader
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.arena.minestom.platform.schematic.MinestomSchematic
import org.readutf.game.engine.arena.ArenaManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.cli.ConsoleActor
import revxrsal.commands.command.CommandActor
import java.io.File
import java.io.FileInputStream

class ArenaCommand(
    workDir: File,
    private val arenaManager: ArenaManager<MinestomWorld>,
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
                SpongeSchematicReader().read(input.readBytes())
            }

        val minestomSchematic = MinestomSchematic(schematic)

        val template = arenaManager.saveTemplate(name, minestomSchematic).getOrElse {
            actor.error("Failed to save template: $it")
            return
        }

        actor.reply("&aSaved template (points: ${template.positions.size})")
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
