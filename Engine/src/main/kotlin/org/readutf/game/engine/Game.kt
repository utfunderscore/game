package org.readutf.game.engine

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.impl.GameTeamAddEvent
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import java.util.UUID

open class Game<ARENA : Arena<*>> {
    private val logger = KotlinLogging.logger { }

    private val gameId = UUID.randomUUID()

    // Required game settings / features
    var stageCreators: MutableList<StageCreator> = mutableListOf()
    var currentStageIndex: Int = 0
    var currentStage: Stage? = null
    var respawnHandler: RespawnHandler? = null
    var arena: ARENA? = null
    private var teams: MutableSet<GameTeam> = mutableSetOf()
    private var gameState: GameState = GameState.STARTUP

    /**
     * Adds players to a team, invokes the GameTeamAddEvent,
     * and teleports them to their spawn
     */
    fun addTeam(team: GameTeam): Result<Unit> {
        logger.info { "Adding team $team to game ($gameId)" }

        teams.add(team)

        if (callEvent(GameTeamAddEvent(this))) {
            return Result.empty()
        }

        if (gameState == GameState.ACTIVE) {
            team.getOnlinePlayers().forEach { player ->
                spawnPlayer(player).onFailure { return@addTeam Result.failure(it) }
            }
        }
        return Result.empty()
    }

    fun start(): Result<Unit> {
        logger.info { "Starting game" }

        if (arena == null) return Result.failure("No arena is active")
        if (respawnHandler == null) return Result.failure("No spawning handler has been defined.")

        getOnlinePlayers().forEach(::spawnPlayer)

        return Result.success(Unit)
    }

    fun end(): Result<Unit> {
        if (gameState != GameState.ACTIVE) {
            return Result.failure("GameState is not active")
        }

        arena?.free()

        return Result.empty()
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

    fun callEvent(event: GameEvent): Boolean {
        GameEventManager.callEvent(event, this)
        return event.cancelled
    }

    fun spawnPlayer(player: Player): Result<Unit> {
        logger.info { "Spawning player $player" }

        val spawnHandling = respawnHandler ?: return Result.failure("No spawning handler has been declared.")

        val (position, instance, _) =
            spawnHandling
                .getRespawnLocation(player)
                .onFailure { return Result.failure(it) }

        player.setInstance(instance, position.toVec())

        return Result.success(Unit)
    }

    fun getPlayers(): List<UUID> = teams.flatMap { it.players }

    fun getOnlinePlayers(): List<Player> {
        println(getPlayers())
        return getPlayers().mapNotNull {
            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it)
        }
    }

    fun getTeam(playerId: UUID): GameTeam? = teams.find { it.players.contains(playerId) }

    fun getTeamId(team: GameTeam): Int = teams.indexOf(team)

    fun getArena(): Result<ARENA> = arena?.let { Result.success(it) } ?: Result.failure("No arena is active")
}
