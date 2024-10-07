package org.readutf.game.server

import io.github.togar2.pvp.MinestomPvP
import io.github.togar2.pvp.feature.CombatFeatureSet
import io.github.togar2.pvp.feature.CombatFeatures
import net.minestom.server.MinecraftServer
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.store.schematic.polar.FilePolarStore
import org.readutf.game.engine.arena.store.template.impl.FileTemplateStore
import org.readutf.game.engine.debug.commands.GameDumpCommand
import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.PositionSettingsManager
import org.readutf.game.engine.settings.store.impl.YamlSettingsStore
import org.readutf.game.server.commands.GameCommand
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.commands.complter.GameCompleterFactory
import org.readutf.game.server.game.GameTypeManager
import org.readutf.game.server.game.dual.DualGamePositions
import org.readutf.game.server.game.dual.stages.AwaitingPlayersSettings
import org.readutf.game.server.world.WorldManager
import revxrsal.commands.minestom.MinestomLamp
import java.io.File

class GameServer {
    private val workDir = File(System.getProperty("user.dir"))

    init {
        println(workDir.path)
    }

    private val server = MinecraftServer.init()
    private val worldManager = WorldManager()
    private val gameSettingsManager = GameSettingsManager(YamlSettingsStore(workDir))
    private val templateStore = FileTemplateStore(workDir)
    private val schematicStore = FilePolarStore(workDir)
    private val positionSettingsManager = PositionSettingsManager()
    private val arenaManager = ArenaManager(positionSettingsManager, templateStore, schematicStore)
    private val gameTypeManager = GameTypeManager(arenaManager)

    init {
        positionSettingsManager.registerRequirements("dual", DualGamePositions::class)
        gameSettingsManager.setDefaultSettings("dual", AwaitingPlayersSettings())
    }

    init {
        listOf(
            GamemodeCommand(),
        ).forEach {
            MinecraftServer.getCommandManager().register(it)
        }

        MinestomPvP.init()

        val modernVanilla: CombatFeatureSet = CombatFeatures.modernVanilla()
        MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode())

        val commandManager =
            MinestomLamp
                .builder()
                .suggestionProviders {
                    it.addProviderFactory(GameCompleterFactory())
                }.build()

        commandManager
            .register(GameDumpCommand())

        commandManager.register(GameCommand(gameTypeManager))

        server.start("0.0.0.0", 25566)
    }
}
