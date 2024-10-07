package org.readutf.game.engine

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.gson.annotations.Expose
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.annotation.scan
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.event.impl.GameLeaveEvent
import org.readutf.game.engine.event.impl.GameRespawnEvent
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.schedular.GameScheduler
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.VectorDeserializer
import org.readutf.game.engine.utils.VectorSerializer
import java.util.UUID
import java.util.function.Predicate
import kotlin.reflect.KClass

open class Game<ARENA : Arena<*>> {
    private val logger = KotlinLogging.logger { }
    val gameId: UUID = UUID.randomUUID()

    // Required game settings / features
    private var stageCreators: ArrayDeque<StageCreator<ARENA>> = ArrayDeque()

    @Expose
    val scheduler = GameScheduler(this)

    @Expose
    var currentStage: Stage? = null

    @Expose
    var respawnHandler: RespawnHandler? = null

    @Expose
    var arena: ARENA? = null
        private set

    private var teams = mutableMapOf<String, GameTeam>()

    @Expose
    var gameState: GameState = GameState.STARTUP

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun registerTeam(teamName: String): Result<Unit> {
        logger.info { "Adding team $teamName to game ($gameId)" }
        if (teams.containsKey(teamName)) return Result.failure("Team already exists")

        teams[teamName] = GameTeam(teamName, mutableListOf())

        return Result.empty()
    }

    fun start(): Result<Unit> {
        logger.info { "Starting game" }
        GameManager.activeGames.add(this)
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

        val listeners = scan(nextStage).mapError { return it }
        listeners.forEach {
            GameEventManager.registerListener(
                this,
                it.key as KClass<out Event>,
                it.value,
            )
        }

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

        val respawnResult = spawnHandling.getRespawnLocation(player)

        val event = GameEventManager.callEvent(GameRespawnEvent(this, player, respawnResult), this)

        val (position, instance, _) = event.respawnPositionResult.mapError { return it }

        if (player.instance != instance) {
            player.setInstance(instance, position)
        } else {
            player.teleport(Pos.fromPoint(position))
        }

        return Result.success(Unit)
    }

    fun getPlayers(): List<UUID> = teams.values.flatMap { it.players }

    fun getOnlinePlayers(): List<Player> =
        getPlayers().mapNotNull {
            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it)
        }

    fun addPlayer(
        player: Player,
        team: GameTeam,
    ): Result<Unit> {
        logger.info { "Adding player $player to team ${team.gameName}" }

        GameManager.playerToGame[player.uuid] = this
        GameEventManager.callEvent(GameJoinEvent(this, player), this)

        team.players.add(player.uuid)

        spawnPlayer(player).onFailure { return it }

        return Result.empty()
    }

    fun addPlayer(
        player: Player,
        teamName: String,
    ): Result<Unit> {
        val team = teams[teamName] ?: return Result.failure("Team $teamName does not exist")
        return addPlayer(player, team)
    }

    fun addPlayer(
        player: Player,
        predicate: Predicate<GameTeam>,
    ): Result<Unit> {
        val team =
            teams.values.firstOrNull { predicate.test(it) }
                ?: throw IllegalArgumentException("No team found for predicate")

        return addPlayer(player, team)
    }

    fun removePlayer(player: Player): Result<Unit> {
        logger.info { "Removing player $player" }

        val team = getTeam(player.uuid) ?: return Result.failure("Player is not in a team")

        team.players.remove(player.uuid)

        GameEventManager.callEvent(GameLeaveEvent(this, player), this)

        return Result.empty()
    }

    fun getTeam(playerId: UUID): GameTeam? = teams.values.find { it.players.contains(playerId) }

    fun getTeam(teamName: String) = teams[teamName]

    fun getTeams(): List<GameTeam> = teams.values.toList()

    fun getArena(): Result<ARENA> = arena?.let { Result.success(it) } ?: Result.failure("No arena is active")

    fun messageAll(component: Component) {
        for (onlinePlayer in getOnlinePlayers()) {
            onlinePlayer.sendMessage(component)
        }
    }

    fun getTeamId(team: GameTeam): Int = teams.values.indexOf(team)

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
