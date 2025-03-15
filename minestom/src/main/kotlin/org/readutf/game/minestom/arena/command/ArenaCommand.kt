package org.readutf.game.minestom.arena.command

import com.github.michaelbull.result.onFailure
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.SpongeSchematic
import net.hollowcube.schem.reader.SpongeSchematicReader
import org.readutf.game.minestom.arena.MinestomArenaManager
import org.readutf.game.minestom.selection.SelectionManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.minestom.actor.MinestomCommandActor
import java.io.File

class ArenaCommand(
    private val schematicsFile: File,
    private val arenaManager: MinestomArenaManager,
    private val selectionManager: SelectionManager,
) {
    private val logger = KotlinLogging.logger { }

    init {
        schematicsFile.mkdirs()
    }

    @Command("arena create <name> <schematic> <gametype>")
    fun create(
        actor: MinestomCommandActor,
        name: String,
        @Named("schematic") schematicName: String,
        @Named("gametype") gameType: String,
    ) {
        val player =
            actor.asPlayer() ?: let {
                actor.reply("Only players can create arenas")
                return
            }

        val schematicFile = File(schematicsFile, "$schematicName.schem")
        if (!schematicFile.exists()) {
            actor.reply("&cSchematic $schematicName does not exist")
            return
        }
        if (!schematicFile.isFile) {
            actor.reply("&cSchematic $schematicName is not a file")
            return
        }

        val schematic = SpongeSchematicReader().read(schematicFile.readBytes()) as SpongeSchematic

        arenaManager.createArena(name, schematic, gameType).thenAccept {
            it.onFailure { error ->
                actor.reply("&cFailed to create arena: $error")
                return@thenAccept
            }
            actor.reply("&aCreated arena $name")
        }
    }
}
