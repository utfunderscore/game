package org.readutf.game.engine.team

import org.readutf.game.engine.utils.SResult
import java.util.UUID

fun interface TeamSelector<T> {

    fun getTeam(playerId: UUID): SResult<T>
}
