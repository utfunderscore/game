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
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.engine.dual.DualGamePositions
import org.readutf.game.server.engine.dual.DualGameSettings
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
    private val commandManager = MinestomLamp.builder().build()

    init {
        positionSettingsManager.registerRequirements("dual", DualGamePositions::class)
        gameSettingsManager.setDefaultSettings("dual", DualGameSettings())
    }

    private val arena by lazy {
        arenaManager.loadArena("thebridge", DualGamePositions::class).onFailure { throw Exception(it.getErrorOrNull()) }
    }

    val dualSettings by lazy { gameSettingsManager.getGameSettings<DualGameSettings>("dual") }

    val game by lazy { DualGame(arena, dualSettings) }

    init {
        listOf(
            GamemodeCommand(),
        ).forEach {
            MinecraftServer.getCommandManager().register(it)
        }

        MinestomPvP.init()

        val modernVanilla: CombatFeatureSet = CombatFeatures.modernVanilla()
        MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode())

        commandManager.register(GameDumpCommand())

        server.start("0.0.0.0", 25566)
    }
}
