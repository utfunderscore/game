package org.readutf.game.server

import net.minestom.server.MinecraftServer
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.store.impl.FileTemplateStore
import org.readutf.game.engine.game.settings.GameSettingsManager
import org.readutf.game.server.commands.ArenaCommand
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.world.WorldManager
import revxrsal.commands.cli.ConsoleCommandHandler
import java.io.File

class GameServer {
    private val workDir = File(System.getProperty("user.dir"))
    private val server = MinecraftServer.init()
    private val worldManager = WorldManager()
    private val gameSettingsManager = GameSettingsManager(workDir)
    private val templateStore = FileTemplateStore(workDir)
    private val arenaManager = ArenaManager(gameSettingsManager, templateStore)

    private val commandManager = ConsoleCommandHandler.create()

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

        server.start("0.0.0.0", 25565)
    }
}
