package org.readutf.game.server

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.AddEntityToInstanceEvent
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.store.schematic.polar.FilePolarStore
import org.readutf.game.engine.arena.store.template.impl.FileTemplateStore
import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.test.DualGamePositions
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.addListener
import org.readutf.game.server.commands.ArenaCommand
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.game.dual.DualGame
import org.readutf.game.server.game.dual.DualGameSettings
import org.readutf.game.server.world.WorldManager
import revxrsal.commands.cli.ConsoleCommandHandler
import java.io.File

class GameServer {
    private val workDir = File(System.getProperty("user.dir"))

    init {
        println(workDir.path)
    }

    private val server = MinecraftServer.init()
    private val worldManager = WorldManager()
    private val gameSettingsManager = GameSettingsManager(workDir)
    private val templateStore = FileTemplateStore(workDir)
    private val schematicStore = FilePolarStore(workDir)
    private val arenaManager = ArenaManager(gameSettingsManager, templateStore, schematicStore)

    private val commandManager = ConsoleCommandHandler.create()

    private val arena = arenaManager.loadArena("thebridge", DualGamePositions::class).onFailure { throw Exception(it.getErrorOrNull()) }

    val game = DualGame(arena, DualGameSettings(2, 2, 10))

    init {
        game.start().onFailure { throw Exception(it.getErrorOrNull()) }
    }

    init {
        listOf(
            GamemodeCommand(),
        ).forEach {
            MinecraftServer.getCommandManager().register(it)
        }

        commandManager.register(ArenaCommand(workDir, arenaManager))

        Thread {
            commandManager.pollInput()
        }.start()

        MinecraftServer.getGlobalEventHandler().addListener<AddEntityToInstanceEvent> { e ->
            if (e.instance != worldManager.instanceContainer) return@addListener

            MinecraftServer.getSchedulerManager().scheduleNextTick {
                val entity = e.entity
                if (entity !is Player) return@scheduleNextTick

                game.addTeam(GameTeam(entity.uuid))
            }
        }

        server.start("0.0.0.0", 25566)
    }
}
