package org.readutf.game.engine.respawning.impl

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.respawning.RespawnPosition
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.types.Result

class TeamIdSpawnHandler(
    val game: Game<*>,
    private val positionPrefix: String,
) : RespawnHandler {
    override fun getRespawnLocation(player: Player): Result<RespawnPosition> {
        val arena = game.getArena().mapError { return it }
        val team = game.getTeam(player.uuid) ?: return Result.failure("Player is not on a team")
        val teamId = game.getTeamId(team)

        val spawn: Point =
            arena.positions
                .filterKeys {
                    it.startsWith("$positionPrefix:${teamId + 1}")
                }.minByOrNull { entry ->
                    arena.instance.getNearbyEntities(entry.value.position, 3.0).count { entity -> entity is Player }
                }?.value
                ?.position ?: return Result.failure("No spawn found for team $teamId")

        return Result.success(RespawnPosition(spawn.add(0.5, 0.0, 0.5), arena.instance, false))
    }
}

fun Game<*>.registerTeamIdSpawnHandler(positionPrefix: String) {
    respawnHandler = TeamIdSpawnHandler(this, positionPrefix)
}

fun Stage.registerTeamIdSpawnHandler(positionPrefix: String) {
    game.respawnHandler = TeamIdSpawnHandler(game, positionPrefix)
}
