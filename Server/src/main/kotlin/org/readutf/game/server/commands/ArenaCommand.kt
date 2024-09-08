package org.readutf.game.server.commands

import net.hollowcube.schem.SchematicReader
import org.readutf.game.engine.arena.ArenaManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.command.CommandActor
import java.io.File
import java.io.FileInputStream

@Command("arena")
class ArenaCommand(
    private val workDir: File,
    private val arenaManager: ArenaManager,
) {
    private val schematicFolder = File(workDir, "schematics")

    init {
        if (!schematicFolder.exists()) {
            schematicFolder.mkdirs()
        }
        if (!schematicFolder.isDirectory) {
            throw IllegalStateException("Schematics folder is not a directory")
        }
    }

    @Subcommand("create")
    fun create(
        actor: CommandActor,
        name: String,
        schematicName: String,
        gameType: String,
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

        val template = arenaManager.createArena(name, schematic, gameType).onFailure { throw Exception(it) }

        actor.reply("&aSaved template (points: ${template.position.size})")
    }
}
