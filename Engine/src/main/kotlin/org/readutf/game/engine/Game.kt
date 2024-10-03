package org.readutf.game.engine

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.impl.GameTeamAddEvent
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.schedular.GameScheduler
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.VectorDeserializer
import org.readutf.game.engine.utils.VectorSerializer
import java.util.UUID

open class Game<ARENA : Arena<*>> {
    private val logger = KotlinLogging.logger { }
    private val gameId = UUID.randomUUID()

    // Required game settings / features
    private var stageCreators: ArrayDeque<StageCreator<ARENA>> = ArrayDeque()
    val scheduler by lazy { GameScheduler(this) }
    var currentStage: Stage? = null
    var respawnHandler: RespawnHandler? = null
    var arena: ARENA? = null
        private set
    private var teams: MutableSet<GameTeam> = mutableSetOf()
    private var gameState: GameState = GameState.STARTUP

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun addTeam(team: GameTeam): Result<Unit> {
        logger.info { "Adding team $team to game ($gameId)" }

        teams.add(team)

        if (callEvent(GameTeamAddEvent(this)).isCancelled()) {
            return Result.failure("GameTeamAddEvent was cancelled")
        }

        if (gameState == GameState.ACTIVE) {
            team.getOnlinePlayers().forEach { player ->
                spawnPlayer(player).mapError { return it }
            }
        }
        return Result.empty()
    }

    fun start(): Result<Unit> {
        logger.info { "Starting game" }
        if (gameState != GameState.STARTUP) {
            return Result.failure("Game is not in startup state")
        }
        startNextStage().mapError { return it }

        if (arena == null) return Result.failure("No arena is active")

        if (respawnHandler == null) return Result.failure("No spawning handler has been defined.")
        getOnlinePlayers().forEach(::spawnPlayer)
        currentStage?.onStart(null)
        gameState = GameState.ACTIVE
        return Result.success(Unit)
    }

    fun startNextStage(): Result<Stage> {
        logger.info { "Starting next stage" }

        val localCurrentStage = currentStage
        if (localCurrentStage != null) {
            localCurrentStage.unregisterListeners()
            localCurrentStage.onFinish().mapError { return it }
        }

        val previous = currentStage

        val nextStageCreator = stageCreators.removeFirstOrNull() ?: return Result.failure("No more stages to start")
        val nextStage = nextStageCreator.create(this).mapError { return it }

        currentStage = nextStage
        nextStage.onStart(previous).mapError { return it }

        return Result.success(currentStage!!)
    }

    fun end(): Result<Unit> {
        if (gameState != GameState.ACTIVE) {
            return Result.failure("GameState is not active")
        }

        arena?.free()

        return Result.empty()
    }

    fun crash(result: Result<*>) {
        arena?.free()

        result.debug { }
        logger.error { "Game $gameId crashed: ${result.getErrorOrNull() ?: "Unknown Reason"}" }

        getOnlinePlayers().forEach { onlinePlayer ->
            onlinePlayer.sendMessage("Game crashed")
        }

        throw Exception("Game crashed")
    }

    fun changeArena(arena: ARENA) {
        logger.info { "Changing to arena $arena" }

        this.arena = arena

        if (gameState == GameState.ACTIVE) {
            for (onlinePlayer in getOnlinePlayers()) {
                spawnPlayer(onlinePlayer)
            }
        }
    }

    fun registerStage(vararg stageCreator: StageCreator<ARENA>) {
        stageCreators.addAll(stageCreator)
    }

    private fun <T : GameEvent> callEvent(event: T): T {
        GameEventManager.callEvent(event, this)
        return event
    }

    fun spawnPlayer(player: Player): Result<Unit> {
        logger.info { "Spawning player $player" }

        val spawnHandling = respawnHandler ?: return Result.failure("No spawning handler has been declared.")

        val (position, instance, _) =
            spawnHandling
                .getRespawnLocation(player)
                .mapError { return it }

        if (player.instance != instance) {
            player.setInstance(instance, position)
        } else {
            player.teleport(Pos.fromPoint(position))
        }

        return Result.success(Unit)
    }

    fun getPlayers(): List<UUID> = teams.flatMap { it.players }

    fun getOnlinePlayers(): List<Player> =
        getPlayers().mapNotNull {
            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it)
        }

    fun getTeam(playerId: UUID): GameTeam? = teams.find { it.players.contains(playerId) }

    fun getTeamId(team: GameTeam): Int = teams.indexOf(team)

    fun getArena(): Result<ARENA> = arena?.let { Result.success(it) } ?: Result.failure("No arena is active")

    fun messageAll(component: Component) {
        for (onlinePlayer in getOnlinePlayers()) {
            onlinePlayer.sendMessage(component)
        }
    }

    companion object {
        internal val objectMapper =
            jsonMapper {
                addModule(kotlinModule())
                enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                val module = SimpleModule()
                module.addSerializer(Point::class.java, VectorSerializer())
                module.addDeserializer(Point::class.java, VectorDeserializer())
                addModule(module)
            }
    }
}
