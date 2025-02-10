package org.readutf.game.engine

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.impl.*
import org.readutf.game.engine.event.listener.RegisteredListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.schedular.GameSchedulerFactory
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.team.TeamSelector
import org.readutf.game.engine.utils.SResult
import java.util.UUID
import kotlin.jvm.Throws

typealias GenericGame = Game<*, *>

abstract class Game<ARENA : Arena<*>, TEAM : GameTeam>(
    private val schedulerFactory: GameSchedulerFactory,
    val eventManager: GameEventManager,
    var teamSelector: TeamSelector<TEAM>,
) {
    private val logger = KotlinLogging.logger { }
    val gameId: String = GameManager.generateGameId()
    private var stageCreators: ArrayDeque<StageCreator<ARENA, TEAM>> = ArrayDeque()
    var currentStage: Stage<ARENA, TEAM>? = null
    var arena: ARENA? = null
    private var teams = LinkedHashMap<String, TEAM>()
    var gameState: GameState = GameState.STARTUP
    val scheduler by lazy { schedulerFactory.build(this) }

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun registerTeam(team: TEAM): SResult<Unit> {
        val teamName = team.teamName
        logger.info { "Adding team $teamName to game ($gameId)" }
        if (teams.containsKey(teamName)) {
            logger.error { "Team already exists with the name $teamName" }
            return Err("Team already exists with the name $teamName")
        }

        teams[teamName] = team

        return Ok(Unit)
    }

    fun start(): SResult<Unit> {
        logger.info { "Starting game" }
        GameManager.activeGames[gameId] = this
        if (gameState != GameState.STARTUP) {
            logger.error { "Game is not in startup state" }
            return Err("Game is not in startup state")
        }
        startNextStage().getOrElse {
            return Err(it)
        }

        if (arena == null) {
            logger.error { "No arena is active" }
            return Err("No arena is active")
        }

        currentStage?.onStart()
        gameState = GameState.ACTIVE
        return Ok(Unit)
    }

    fun startNextStage(): SResult<Stage<ARENA, TEAM>> {
        val nextStageCreator = stageCreators.removeFirstOrNull() ?: let {
            logger.error { "No more stages to start" }
            return Err("No more stages to start")
        }

        return startNextStage(nextStageCreator)
    }

    fun startNextStage(nextStageCreator: StageCreator<ARENA, TEAM>): SResult<Stage<ARENA, TEAM>> {
        logger.info { "Starting next stage..." }

        val localCurrentStage = currentStage
        if (localCurrentStage != null) {
            localCurrentStage.unregisterListeners()
            localCurrentStage.onFinish().getOrElse {
                return Err(it)
            }
        }

        val previous = currentStage
        val nextStage = nextStageCreator.create(this, previous).getOrElse { return Err(it) }

        logger.info { "Starting stage ${nextStage.javaClass.simpleName}" }

        currentStage = nextStage

        callEvent(StageStartEvent(nextStage, previous))

        nextStage.onStart().getOrElse {
            return Err(it)
        }

        return Ok(currentStage!!)
    }

    fun end(): SResult<Unit> {
        callEvent(GameEndEvent(this))

        if (gameState != GameState.ACTIVE) {
            logger.error { "GameState is not active" }
            return Err("GameState is not active")
        }

        arena?.free()

        return Ok(Unit)
    }

    @Throws(Exception::class)
    fun crash(result: SResult<*>?) {
        callEvent(GameCrashEvent(this))

        arena?.free()

        logger.error { "Game $gameId crashed: ${result?.getOr(null) ?: "Unknown Reason"}" }

        throw Exception("Game crashed")
    }

    fun <T : Feature> addFeature(feature: T): T {
        for ((type, listener) in feature.getListeners().toList()) {
            eventManager.registerListener(
                this,
                type,
                RegisteredListener(
                    gameListener = listener,
                    ignoreCancelled = true,
                    ignoreSpectators = false,
                    priority = 50,
                ),
            )
        }

        for (task in feature.getTasks()) {
            scheduler.schedule(task)
        }

        return feature
    }

    inline fun <reified T : Any> registerListener(typedGameListener: TypedGameListener<T>) {
        eventManager.registerListener(
            this,
            T::class,
            RegisteredListener(
                gameListener = typedGameListener,
                ignoreCancelled = true,
                ignoreSpectators = false,
                priority = 0,
            ),
        )
    }

    fun changeArena(arena: ARENA) {
        logger.info { "Changing to arena $arena" }

        val previousArena = this.arena
        this.arena = arena

        callEvent(GameArenaChangeEvent(this, arena, previousArena))
    }

    fun registerStage(vararg stageCreator: StageCreator<ARENA, TEAM>) {
        stageCreators.addAll(stageCreator)
    }

    fun <T : GameEvent> callEvent(event: T): T {
        eventManager.callEvent(event, this)
        return event
    }

    fun getPlayers(): List<UUID> = teams.values.flatMap { it.players }

    fun getOnlinePlayers(): List<UUID> = getPlayers().filter { isOnline(it) }

    abstract fun isOnline(playerId: UUID): Boolean

    abstract fun messagePlayer(playerId: UUID, component: Component)

    fun addPlayer(
        playerId: UUID,
    ): SResult<Unit> {
        val team = teamSelector.getTeam(playerId).getOrElse {
            return Err(it)
        }
        logger.info { "Adding player $playerId to team ${team.teamName}" }

        GameManager.playerToGame[playerId] = this
        team.players.add(playerId)
        teams[team.teamName] = team

        callEvent(GameJoinEvent(this, playerId))

        return Ok(Unit)
    }

    fun removePlayer(playerId: UUID): SResult<Unit> {
        logger.info { "Removing player $playerId" }

        val team = getTeam(playerId) ?: let {
            logger.error { "GamePlayer is not in a team" }
            return Err("GamePlayer is not in a team")
        }
        team.players.remove(playerId)
        callEvent(GameLeaveEvent(this, playerId))

        return Ok(Unit)
    }

    fun getTeam(playerId: UUID): TEAM? = teams.values.find { it.players.contains(playerId) }

    fun getTeam(teamName: String): TEAM? = teams.entries.firstOrNull { entry -> entry.key.equals(teamName, true) }?.value

    fun getTeams(): List<TEAM> = teams.values.toList()

    fun getArena(): SResult<ARENA> = arena?.let { Ok(it) } ?: let {
        logger.error { "No arena is active" }
        return Err("No arena is active")
    }

    fun messageAll(component: Component) {
        for (onlinePlayer in getOnlinePlayers()) {
            messagePlayer(onlinePlayer, component)
        }
    }

    fun getTeamId(team: GameTeam): Int = teams.values.indexOf(team)
}
