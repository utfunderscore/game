package org.readutf.game.server.game.dual.stages

import org.readutf.arena.minestom.platform.MinestomArena
import org.readutf.arena.minestom.platform.MinestomStage
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.game.engine.Game
import org.readutf.game.engine.team.GameTeam

class VictoryStage<ARENA : MinestomArena<*>, TEAM : GameTeam>(
    game: Game<MinestomWorld, ARENA, TEAM>,
    previousStage: VictoryStage<ARENA, TEAM>?,
    winner: GameTeam?,
) : MinestomStage<TEAM>(game, previousStage)
