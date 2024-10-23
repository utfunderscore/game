package org.readutf.game.server.game.impl

import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.features.combat.enableDamageTracker
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.server.game.dual.stages.AwaitingPlayersStage
import org.readutf.game.server.game.impl.settings.TheBridgePositions
import org.readutf.game.server.game.impl.settings.TheBridgeSettings
import java.util.UUID

class TheBridgeGame(
    arena: Arena<TheBridgePositions>,
    val settings: TheBridgeSettings,
    kitManager: KitManager,
) : Game<Arena<TheBridgePositions>, TheBridgeTeam>() {
    val kit = kitManager.loadKit("thebridge").getOrThrow()

    var round = 0
    val damageTracker = enableDamageTracker()
    val teamHealths = mutableMapOf<String, Int>()
    private val goalsScored = mutableMapOf<UUID, Int>()

    init {
        changeArena(arena)

        registerTeam(TheBridgeTeam("red", NamedTextColor.RED))
        registerTeam(TheBridgeTeam("blue", NamedTextColor.BLUE))

        val goalTeams = mapOf<TheBridgeTeam, Cuboid>()

        val goals =
            arena.positionSettings
                .getPortals(
                    getTeams().map { it as TheBridgeTeam },
                ).getOrThrow()

        val safeZones =
            arena.positionSettings
                .getSafezones(
                    getTeams().map { it.teamName.lowercase() },
                ).getOrThrow()

        val stages: List<StageCreator<Arena<TheBridgePositions>, TheBridgeTeam>> =
            List(20) {
                StageCreator { game, previousStage ->
                    TheBridgeStage(game as TheBridgeGame, previousStage, goals, safeZones).toSuccess()
                }
            }

        registerStage(
            AwaitingPlayersStage.Creator(settings.awaitingPlayersSettings, arena.positionSettings.dualGamePositions),
            *stages.toTypedArray(),
        )
    }

    fun getTeamHealth(team: GameTeam): Int = teamHealths.getOrPut(team.teamName) { settings.numberOfLives }

    fun decreaseHealth(team: GameTeam) {
        teamHealths[team.teamName] = getTeamHealth(team) - 1
    }

    fun getGoals(player: Player): Int = goalsScored[player.uuid] ?: 0

    fun scoredGoal(player: Player) {
        goalsScored.compute(player.uuid) { _, value ->
            if (value == null) 1 else value + 1
        }
    }
}
