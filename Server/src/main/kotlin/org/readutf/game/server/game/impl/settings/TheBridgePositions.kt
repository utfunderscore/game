package org.readutf.game.server.game.impl.settings

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.Position
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.server.game.dual.DualGamePositions
import org.readutf.game.server.game.impl.TheBridgeTeam

class TheBridgePositions(
    @Position(startsWith = "goal") val goalMarkers: List<Marker>,
    @Position(startsWith = "safezone") val safezone: List<Marker>,
    @Position val dualGamePositions: DualGamePositions,
) : PositionData {
    private val logger = KotlinLogging.logger {}

    fun getPortals(teams: List<TheBridgeTeam>): Result<Map<TheBridgeTeam, Cuboid>> {
        val goals = mutableMapOf<TheBridgeTeam, Cuboid>()

        for (team in teams) {
            val points = goalMarkers.filter { it.markerName.startsWith("goal:${team.teamName.lowercase()}") }
            if (points.count() < 2) {
                logger.error { "No goal points found for team $team" }
                return Result.failure("No goal points found for team $team")
            }

            val cuboid = Cuboid.fromVecs(points.first().position, points.last().position)

            goals[team] = cuboid
        }

        return Result.success(goals)
    }

    fun getSafezones(teams: List<String>): Result<Map<String, Cuboid>> {
        val goals = mutableMapOf<String, Cuboid>()

        for (team in teams) {
            val points = safezone.filter { it.markerName.startsWith("safezone:$team") }
            if (points.count() < 2) {
                logger.error { "No safe zones found for team $team" }
                return Result.failure("No safe zones found for team $team")
            }

            val cuboid = Cuboid.fromVecs(points.first().position, points.last().position)

            goals[team] = cuboid
        }

        return Result.success(goals)
    }
}
