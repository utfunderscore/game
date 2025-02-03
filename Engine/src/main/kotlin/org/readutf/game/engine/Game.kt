package org.readutf.game.engine

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.impl.*
import org.readutf.game.engine.schedular.GameScheduler
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import java.util.UUID
import java.util.function.Predicate
import kotlin.jvm.Throws

typealias GenericGame = Game<*, *>

abstract class Game<ARENA : Arena<*>, TEAM : GameTeam>(
    val scheduler: GameScheduler,
    val eventManager: GameEventManager,
) {
    private val logger = KotlinLogging.logger { }
    val gameId: String = GameManager.generateGameId()
    private var stageCreators: ArrayDeque<StageCreator<ARENA, TEAM>> = ArrayDeque()
    var currentStage: Stage<ARENA, TEAM>? = null
    var arena: ARENA? = null
    private var teams = LinkedHashMap<String, TEAM>()
    var gameState: GameState = GameState.STARTUP

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun registerTeam(team: TEAM): Result<Unit> {
        val teamName = team.teamName
        logger.info { "Adding team $teamName to game ($gameId)" }
        if (teams.containsKey(teamName)) return Result.failure("Team already exists with the name $teamName")

        teams[teamName] = team

        return Result.empty()
    }

    fun start(): Result<Unit> {
        logger.info { "Starting game" }
        GameManager.activeGames[gameId] = this
        if (gameState != GameState.STARTUP) {
            return Result.failure("Game is not in startup state")
        }
        startNextStage().mapError { return it }

        if (arena == null) return Result.failure("No arena is active")

        currentStage?.onStart()
        gameState = GameState.ACTIVE
        return Result.success(Unit)
    }

    fun startNextStage(): Result<Stage<ARENA, TEAM>> {
        logger.info { "Starting next stage" }

        val nextStageCreator = stageCreators.removeFirstOrNull() ?: return Result.failure("No more stages to start")

        return startNextStage(nextStageCreator)
    }

    fun startNextStage(nextStageCreator: StageCreator<ARENA, TEAM>): Result<Stage<ARENA, TEAM>> {
        val localCurrentStage = currentStage
        if (localCurrentStage != null) {
            localCurrentStage.unregisterListeners()
            localCurrentStage.onFinish().mapError { return it }
        }

        val previous = currentStage
        val nextStage = nextStageCreator.create(this, previous).mapError { return it }
        currentStage = nextStage

        callEvent(StageStartEvent(nextStage, previous))

        nextStage.onStart().mapError { return it }

        return Result.success(currentStage!!)
    }

    fun end(): Result<Unit> {
        callEvent(GameEndEvent(this))

        if (gameState != GameState.ACTIVE) {
            return Result.failure("GameState is not active")
        }

        arena?.free()

        return Result.empty()
    }

    @Throws(Exception::class)
    fun crash(result: Result<*>?) {
        callEvent(GameCrashEvent(this))

        arena?.free()

        result?.debug { }
        logger.error { "Game $gameId crashed: ${result?.getErrorOrNull() ?: "Unknown Reason"}" }

        throw Exception("Game crashed")
    }

    fun changeArena(arena: ARENA) {
        logger.info { "Changing to arena $arena" }

        this.arena = arena

        if (gameState == GameState.ACTIVE) {
            for (onlinePlayer in getOnlinePlayers()) {
//                spawnPlayer(onlinePlayer)
            }
        }
    }

    fun registerStage(vararg stageCreator: StageCreator<ARENA, TEAM>) {
        stageCreators.addAll(stageCreator)
    }

    fun <T : GameEvent> callEvent(event: T): T {
        eventManager.callEvent(event, this)
        return event
    }

    fun getPlayers(): List<UUID> = teams.values.flatMap { it.players }

    abstract fun getOnlinePlayers(): List<UUID>

    abstract fun messagePlayer(playerId: UUID, component: Component)

    private fun addPlayer(
        playerId: UUID,
        team: GameTeam,
    ): Result<Unit> {
        logger.info { "Adding player $playerId to team ${team.teamName}" }

        GameManager.playerToGame[playerId] = this
        team.players.add(playerId)
        callEvent(GameJoinEvent(this, playerId))

        return Result.empty()
    }

    fun addPlayer(
        playerId: UUID,
        teamName: String,
    ): Result<Unit> {
        val team = teams[teamName] ?: return Result.failure("Team $teamName does not exist")
        return addPlayer(playerId, team)
    }

    fun addPlayer(
        player: UUID,
        predicate: Predicate<TEAM>,
    ): Result<Unit> {
        val team =
            teams.values.firstOrNull { predicate.test(it) }
                ?: return Result.failure("No team matches the predicate")

        return addPlayer(player, team)
    }

    fun removePlayer(playerId: UUID): Result<Unit> {
        logger.info { "Removing player $playerId" }

        val team = getTeam(playerId) ?: return Result.failure("GamePlayer is not in a team")
        team.players.remove(playerId)
        callEvent(GameLeaveEvent(this, playerId))

        return Result.empty()
    }

    fun getTeam(playerId: UUID): TEAM? = teams.values.find { it.players.contains(playerId) }

    fun getTeam(teamName: String): TEAM? = teams.entries.firstOrNull { entry -> entry.key.equals(teamName, true) }?.value

    fun getTeams(): List<TEAM> = teams.values.toList()

    fun getArena(): Result<ARENA> = arena?.let { Result.success(it) } ?: Result.failure("No arena is active")

    fun messageAll(component: Component) {
        for (onlinePlayer in getOnlinePlayers()) {
            messagePlayer(onlinePlayer, component)
        }
    }

    fun getTeamId(team: GameTeam): Int = teams.values.indexOf(team)
}
