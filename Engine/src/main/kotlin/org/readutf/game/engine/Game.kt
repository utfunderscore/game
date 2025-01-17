package org.readutf.game.engine

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.annotation.scan
import org.readutf.game.engine.event.impl.*
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.schedular.GameScheduler
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.Position
import org.readutf.game.engine.utils.PositionDeserializer
import org.readutf.game.engine.utils.PositionSerializer
import org.readutf.game.engine.utils.SResult
import java.util.UUID
import java.util.function.Predicate
import kotlin.jvm.Throws

typealias GenericGame = Game<*, *, *>

abstract class Game<WORLD : ArenaWorld, ARENA : Arena<*, WORLD>, TEAM : GameTeam>(private val platform: Platform<*>, val eventManager: GameEventManager) {

    private val logger = KotlinLogging.logger { }
    val gameId: String = GameManager.generateGameId()

    // Required game settings / features
    private var stageCreators: ArrayDeque<StageCreator<WORLD, ARENA, TEAM>> = ArrayDeque()

    val scheduler = GameScheduler(platform)

    var currentStage: Stage<WORLD, ARENA, TEAM>? = null

    var respawnHandler: RespawnHandler? = null

    var arena: ARENA? = null
        private set

    private var teams = LinkedHashMap<String, TEAM>()

    private var gameState: GameState = GameState.STARTUP

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun registerTeam(team: TEAM): SResult<Unit> {
        val teamName = team.teamName
        logger.info { "Adding team $teamName to game ($gameId)" }
        if (teams.containsKey(teamName)) return Err("Team already exists with the name $teamName")

        teams[teamName] = team

        return Ok(Unit)
    }

    fun start(): SResult<Unit> {
        logger.info { "Starting game" }
        GameManager.activeGames[gameId] = this
        if (gameState != GameState.STARTUP) {
            return Err("Game is not in startup state")
        }
        startNextStage().getOrElse { return Err(it) }

        if (arena == null) return Err("No arena is active")

        if (respawnHandler == null) return Err("No spawning handler has been defined.")

        scheduler.startGameThread(this)
        getOnlinePlayers().forEach(::spawnPlayer)
        currentStage?.onStart()
        gameState = GameState.ACTIVE
        return Ok(Unit)
    }

    fun startNextStage(): SResult<Stage<WORLD, ARENA, TEAM>> {
        logger.info { "Starting next stage" }

        val nextStageCreator = stageCreators.removeFirstOrNull() ?: return Err("No more stages to start")

        return startNextStage(nextStageCreator)
    }

    fun startNextStage(nextStageCreator: StageCreator<WORLD, ARENA, TEAM>): SResult<Stage<WORLD, ARENA, TEAM>> {
        val localCurrentStage = currentStage
        if (localCurrentStage != null) {
            localCurrentStage.unregisterListeners()
            localCurrentStage.onFinish().getOrElse { return Err(it) }
        }

        val previous = currentStage

        val nextStage = nextStageCreator.create(this, previous).getOrElse { return Err(it) }

        val listeners = scan(nextStage).getOrElse { return Err(it) }
        logger.info { "Scan result for ${nextStage::class.simpleName} is $listeners" }
        listeners.forEach {
            eventManager.registerListener(
                this,
                it.key,
                it.value,
            )
        }

        currentStage = nextStage
        nextStage.onStart().getOrElse { return Err(it) }

        return Ok(currentStage!!)
    }

    fun end(): SResult<Unit> {
        eventManager.callEvent(GameEndEvent(this), this)

        if (gameState != GameState.ACTIVE) {
            return Err("GameState is not active")
        }

        arena?.free()

        return Ok(Unit)
    }

    @Throws(Exception::class)
    fun crash(result: SResult<*>?) {
        eventManager.callEvent(GameCrashEvent(this), this)

        arena?.free()

        logger.error { "Game $gameId crashed: ${result?.getError() ?: "Unknown Reason"}" }

        messageAll(Component.text("The game has crashed."))

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

    fun registerStage(vararg stageCreator: StageCreator<WORLD, ARENA, TEAM>) {
        stageCreators.addAll(stageCreator)
    }

    fun <T : GameEvent> callEvent(event: T): T {
        eventManager.callEvent(event, this)
        return event
    }

    fun spawnPlayer(player: GamePlayer): SResult<Unit> {
        logger.info { "Spawning player $player" }

        val spawnHandling = respawnHandler ?: return Err("No spawning handler has been declared.")

        val respawnResult = spawnHandling.getRespawnLocation(player).getOrElse { return Err(it) }

//        player.setTag(VanillaFallFeature.FALL_DISTANCE, 0.0)

        val event = eventManager.callEvent(GameRespawnEvent(this, player, respawnResult), this)

        val (position, instance, _) = event.respawnPositionResult
//        arena!!.instance.loadChunk(position)

        logger.info { "Teleporting player $player to $position in instance $instance" }
        player.teleport(position, instance)

        return Ok(Unit)
    }

    fun getPlayers(): List<UUID> = teams.values.flatMap { it.players }

    fun addPlayer(
        player: GamePlayer,
        team: GameTeam,
    ): SResult<Unit> {
        logger.info { "Adding player $player to team ${team.teamName}" }

        GameManager.playerToGame[player.uuid] = this
        team.players.add(player.uuid)
        eventManager.callEvent(GameJoinEvent(this, player), this)

        spawnPlayer(player).getOrElse { return Err(it) }

        return Ok(Unit)
    }

    fun addPlayer(
        player: GamePlayer,
        teamName: String,
    ): SResult<Unit> {
        val team = teams[teamName] ?: return Err("Team $teamName does not exist")
        return addPlayer(player, team)
    }

    fun addPlayer(
        player: GamePlayer,
        predicate: Predicate<TEAM>,
    ): SResult<Unit> {
        val team =
            teams.values.firstOrNull { predicate.test(it) }
                ?: return Err("No team matches the predicate")

        return addPlayer(player, team)
    }

    fun removePlayer(player: GamePlayer): SResult<Unit> {
        logger.info { "Removing player $player" }

        val team = getTeam(player.uuid) ?: return Err("Player is not in a team")

        team.players.remove(player.uuid)

        eventManager.callEvent(GameLeaveEvent(this, player), this)

        return Ok(Unit)
    }

    fun getOnlinePlayers(): Collection<GamePlayer> = getPlayers().mapNotNull { platform.getPlayer(it) }

    fun getTeam(playerId: UUID): TEAM? = teams.values.find { it.players.contains(playerId) }

    fun getTeam(teamName: String): TEAM? = teams.entries.firstOrNull { entry -> entry.key.equals(teamName, true) }?.value

    fun getTeams(): List<TEAM> = teams.values.toList()

    fun getArena(): SResult<ARENA> = arena?.let { Ok(it) } ?: Err("No arena is active")

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
                module.addSerializer(Position::class.java, PositionSerializer())
                module.addDeserializer(Position::class.java, PositionDeserializer())
                addModule(module)
            }
    }
}
