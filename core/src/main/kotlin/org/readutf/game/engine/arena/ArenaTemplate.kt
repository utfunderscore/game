package org.readutf.game.engine.arena

import org.readutf.buildformat.common.markers.Marker
import org.readutf.buildformat.common.markers.Position

data class ArenaTemplate(
    val name: String,
    val positions: Map<String, Marker>,
    val size: Position,
)
