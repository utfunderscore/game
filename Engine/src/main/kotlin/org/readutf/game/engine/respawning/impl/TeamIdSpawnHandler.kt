package org.readutf.game.engine.respawning.impl

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.respawning.RespawnHandler
import org.readutf.game.engine.respawning.RespawnPosition
import org.readutf.game.engine.stage.GenericStage
import org.readutf.game.engine.utils.Position
import org.readutf.game.engine.utils.SResult

class TeamIdSpawnHandler(
    val game: GenericGame,
    private val positionPrefix: String,
    private val spawnFilter: (Position) -> Int = { 0 },
) : RespawnHandler {
    override fun getRespawnLocation(player: GamePlayer): SResult<RespawnPosition> {
        val arena = game.getArena().getOrElse { return Err(it) }
        val team = game.getTeam(player.uuid) ?: return Err("Player is not on a team")
        val teamId = game.getTeamId(team)

        val spawn: Position =
            arena.positions
                .filterKeys {
                    it.startsWith("$positionPrefix:${teamId + 1}")
                }.minByOrNull { spawnFilter(it.value.position) }?.value
                ?.position ?: return Err("No spawn found for team $teamId")

        return Ok(RespawnPosition(spawn.add(0.5, 0.0, 0.5), arena.arenaWorld, false))
    }
}

fun GenericGame.registerTeamIdSpawnHandler(positionPrefix: String) {
    respawnHandler = TeamIdSpawnHandler(this, positionPrefix)
}

fun GenericStage.registerTeamIdSpawnHandler(positionPrefix: String, filter: (Position) -> Int) {
    game.respawnHandler = TeamIdSpawnHandler(game, positionPrefix, filter)
}
