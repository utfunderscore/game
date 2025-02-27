package org.readutf.game.engine.arena

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.utils.Position

data class ArenaTemplate(
    val name: String,
    val positions: Map<String, Marker>,
    val size: Position,
    val supportedGames: List<String>,
)
