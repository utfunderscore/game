package org.readutf.game.server

import io.github.togar2.pvp.MinestomPvP
import io.github.togar2.pvp.feature.CombatFeatureSet
import io.github.togar2.pvp.feature.CombatFeatures
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.store.schematic.polar.FilePolarStore
import org.readutf.game.engine.arena.store.template.impl.FileTemplateStore
import org.readutf.game.engine.debug.commands.GameDumpCommand
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.kit.command.KitCommand
import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.PositionSettingsManager
import org.readutf.game.engine.settings.store.impl.YamlSettingsStore
import org.readutf.game.server.commands.ArenaCommand
import org.readutf.game.server.commands.GameCommand
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.commands.completions.GameCompleterFactory
import org.readutf.game.server.commands.completions.GameTypeCompleterFactory
import org.readutf.game.server.dev.AutoGameStarter
import org.readutf.game.server.dev.TexturePackManager
import org.readutf.game.server.game.GameTypeManager
import org.readutf.game.server.game.dual.DualGamePositions
import org.readutf.game.server.game.dual.stages.AwaitingPlayersSettings
import org.readutf.game.server.game.impl.TheBridgePositions
import org.readutf.game.server.world.WorldManager
import revxrsal.commands.cli.CLILamp
import revxrsal.commands.cli.ConsoleActor
import revxrsal.commands.cli.actor.ActorFactory
import revxrsal.commands.minestom.MinestomLamp
import java.io.File

class GameServer {
    private val workDir = File(System.getProperty("user.dir"))

    init {
        println(workDir.path)
    }

    private val server = MinecraftServer.init()
    private val worldManager = WorldManager()
    private val kitManager = KitManager(workDir)
    private val gameSettingsManager = GameSettingsManager(YamlSettingsStore(workDir))
    private val templateStore = FileTemplateStore(workDir)
    private val schematicStore = FilePolarStore(workDir)
    private val positionSettingsManager = PositionSettingsManager()
    private val arenaManager = ArenaManager(positionSettingsManager, templateStore, schematicStore)
    private val gameTypeManager = GameTypeManager(arenaManager, kitManager)

    init {
        positionSettingsManager.registerRequirements("dual", DualGamePositions::class)
        positionSettingsManager.registerRequirements("thebridge", TheBridgePositions::class)
        gameSettingsManager.setDefaultSettings("dual", AwaitingPlayersSettings())
    }

    init {
        listOf(
            GamemodeCommand(),
        ).forEach {
            MinecraftServer.getCommandManager().register(it)
        }

        MojangAuth.init()

        MinestomPvP.init()

        val modernVanilla: CombatFeatureSet = CombatFeatures.modernVanilla()
        MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode())

        val commandManager =
            MinestomLamp
                .builder()
                .suggestionProviders {
                    it.addProviderFactory(GameCompleterFactory())
                    it.addProviderFactory(GameTypeCompleterFactory(gameTypeManager))
                }.build()

        val arenaCommand = ArenaCommand(workDir, arenaManager)

        commandManager.register(
            GameDumpCommand(),
            GameCommand(gameTypeManager),
            KitCommand(kitManager),
        )

        val cliCommandManager = CLILamp.builder<ConsoleActor>().build()
        cliCommandManager.register(arenaCommand)
        val consoleActor = ActorFactory.defaultFactory().createForStdIo(cliCommandManager)
        Thread {
            while (true) {
                val line = readlnOrNull()
                if (line != null) {
                    cliCommandManager.dispatch(consoleActor, line)
                }
            }
        }.start()

        server.start("0.0.0.0", 25566)

        AutoGameStarter(gameTypeManager)

        TexturePackManager
    }
}
