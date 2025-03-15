package org.readutf.game.engine.team

import com.github.michaelbull.result.Result
import java.util.UUID

fun interface TeamSelector<T> {

    fun getTeam(playerId: UUID): Result<T, Throwable>
}
