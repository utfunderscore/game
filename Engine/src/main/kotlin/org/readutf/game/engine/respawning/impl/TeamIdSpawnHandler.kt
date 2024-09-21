package org.readutf.game.engine.respawning.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.respawning.RespawnPosition
import org.readutf.game.engine.types.Position
import org.readutf.game.engine.types.Result

class TeamIdSpawnHandler(
    val game: Game<*>,
    private val positionPrefix: String,
) : RespawnHandler {
    override fun getRespawnLocation(player: Player): Result<RespawnPosition> {
        val arena = game.getArena().onFailure { return Result.failure(it) }
        val team = game.getTeam(player.uuid) ?: return Result.failure("Player is not on a team")
        val teamId = game.getTeamId(team)

        val spawn: Position? =
            arena.positions
                .filterKeys {
                    it.startsWith("$positionPrefix:${teamId + 1}")
                }.minByOrNull { entry ->
                    arena.instance.getNearbyEntities(entry.value.toVec(), 3.0).count { entity -> entity is Player }
                }?.value

        if (spawn == null) {
            return Result.failure("No spawn found for team $teamId")
        }

        return Result.success(RespawnPosition(spawn, arena.instance, false))
    }
}

fun Game<*>.registerTeamIdSpawnHandler(positionPrefix: String) {
    respawnHandler = TeamIdSpawnHandler(this, positionPrefix)
}
