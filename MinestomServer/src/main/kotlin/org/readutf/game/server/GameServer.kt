package org.readutf.game.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.togar2.pvp.MinestomPvP
import io.github.togar2.pvp.feature.CombatFeatureSet
import io.github.togar2.pvp.feature.CombatFeatures
import net.bladehunt.kotstom.dsl.listen
import net.minestom.server.MinecraftServer
import net.minestom.server.event.entity.EntitySpawnEvent
import net.minestom.server.extras.MojangAuth
import org.readutf.arena.minestom.arena.store.polar.impl.FilePolarStore
import org.readutf.arena.minestom.itemstack.MinestomItemStackSerializer
import org.readutf.arena.minestom.platform.MinestomPlatform
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.arena.minestom.platform.schematic.SchematicMarkerScanner
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.arena.ArenaCreator
import org.readutf.game.engine.arena.ArenaManager
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.store.template.FileTemplateStore
import org.readutf.game.engine.debug.commands.GameDumpCommand
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.kit.command.KitCommand
import org.readutf.game.engine.settings.GameSettingsManager
import org.readutf.game.engine.settings.PositionSettingsManager
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.settings.store.impl.YamlSettingsStore
import org.readutf.game.server.commands.ArenaCommand
import org.readutf.game.server.commands.GameCommand
import org.readutf.game.server.commands.GamemodeCommand
import org.readutf.game.server.commands.completions.GameCompleterFactory
import org.readutf.game.server.commands.completions.GameTypeCompleterFactory
import org.readutf.game.server.dev.AutoGameStarter
import org.readutf.game.server.game.GameTypeManager
import org.readutf.game.server.game.dual.DualGamePositions
import org.readutf.game.server.game.dual.stages.AwaitingPlayersSettings
import org.readutf.game.server.game.impl.settings.TheBridgePositions
import org.readutf.game.server.metrics.MetricsManager
import revxrsal.commands.cli.CLILamp
import revxrsal.commands.cli.ConsoleActor
import revxrsal.commands.cli.actor.ActorFactory
import revxrsal.commands.minestom.MinestomLamp
import java.io.File
import java.util.*

class GameServer {
    private val workDir = File(System.getProperty("user.dir"))
    private val logger = KotlinLogging.logger {}

    init {
        println(workDir.path)
    }

    private val platform = MinestomPlatform()
    private val server = MinecraftServer.init()
    private val kitManager = KitManager(
        workDir,
        MinestomItemStackSerializer(),
        platform = platform,
    )
    private val gameSettingsManager = GameSettingsManager(YamlSettingsStore(workDir))
    private val templateStore = FileTemplateStore(workDir)
    private val schematicStore = FilePolarStore(workDir)
    private val positionSettingsManager = PositionSettingsManager()
    private val markerScanner = SchematicMarkerScanner()
    private val arenaManager = ArenaManager(
        markerScanner,
        positionSettingsManager,
        templateStore,
        schematicStore,
        arenaCreator = object : ArenaCreator<MinestomWorld> {
            override fun <POSITION : PositionData> create(
                arenaId: UUID,
                positionSettings: POSITION,
                arenaWorld: MinestomWorld,
                positions: Map<String, Marker>,
            ): Arena<POSITION, MinestomWorld> = Arena(arenaId, positionSettings, arenaWorld, positions) {
                logger.info { "Freeing arena $arenaId" }
                MinecraftServer.getInstanceManager().unregisterInstance(arenaWorld.instance)
            }
        },

    )
    private val gameTypeManager = GameTypeManager(arenaManager, kitManager)
    private var metricsManager: MetricsManager? = null

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

        val modernVanilla: CombatFeatureSet =
            CombatFeatures
                .modernVanilla()

        MinecraftServer
            .getGlobalEventHandler()
            .addChild(modernVanilla.createNode())

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
            KitCommand(platform, kitManager),
        )

        MinecraftServer.getGlobalEventHandler().listen<EntitySpawnEvent> {

            println("Entity spawned: ${it.entity}")
        }

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

        Runtime.getRuntime().addShutdownHook(
            Thread {
                metricsManager?.shutdown()
            },
        )
    }
}
