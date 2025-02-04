package org.readutf.game.engine.team

import java.util.UUID

open class GameTeam(
    val teamName: String,
    val players: MutableList<UUID>,
)
