package org.readutf.game.engine.arena

import org.readutf.game.engine.types.Position

data class ArenaTemplate(
    val name: String,
    val position: Map<String, Position>,
    val max: Position,
    val supportedGames: List<String>,
)
