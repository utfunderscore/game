package org.readutf.game.server.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.hollowcube.schem.SchematicReader
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.store.schematic.polar.FilePolarStore
import org.readutf.game.engine.arena.store.schematic.schem.FileSchematicStore
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.command.CommandActor
import java.io.File
import java.io.FileInputStream
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

@Command("arena")
class ArenaCommand(
    private val workDir: File,
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

        val (template, time) =
            measureTimedValue {
                arenaManager.createArena(name, schematic, gameType).onFailure { throw Exception(it.getError()) }
            }

        actor.reply("&aSaved template (points: ${template.positions.size}) in ${time.inWholeMilliseconds}ms")
    }

    @Subcommand("benchmark")
    fun benchmark() {
        val schematicName = "yingyang"

        val schematicFile = File(schematicFolder, "$schematicName.schem")
        if (!schematicFile.exists()) {
            println("Schematic $schematicName does not exist")
            return
        }

        val schematic =
            FileInputStream(schematicFile).use { input ->
                SchematicReader().read(input)
            }

        val baseDir = File(System.getProperty("user.dir"))
        val polarStore = FilePolarStore(baseDir)
        val rawStore = FileSchematicStore(baseDir)

        val time1 =
            measureTimeMillis {
                runBlocking {
                    for (i in 0 until 100) {
                        launch {
                            logger.info { "Loading polar $i" }
                            polarStore.load("benchmark-$i")
                        }
                    }
                }
            }

        val time2 =
            measureTimeMillis {
                runBlocking {
                    for (i in 0 until 100) {
                        async {
                            logger.info { "Loading raw $i (${Thread.currentThread().name})" }
                            rawStore.load("benchmark-$i")
                        }
                    }
                }
            }

        val time3 =
            measureTimeMillis {
                runBlocking {
                    for (i in 0 until 100) {
                        async {
                            logger.info { "Saving polar $i (${Thread.currentThread().name})" }
                            polarStore.save("benchmark-$i", schematic, emptyList())
                        }
                    }
                }
            }

        val time4 =
            measureTimeMillis {
                runBlocking {
                    for (i in 0 until 100) {
                        async {
                            logger.info { "Saving raw $i" }
                            rawStore.save("benchmark-$i", schematic, emptyList())
                        }
                    }
                }
            }

        logger.info { "Load = Polar: $time1, Raw: $time2" }
        logger.info { "Save = Polar: $time3, Raw: $time4" }
    }
}
