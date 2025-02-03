package org.readutf.game.engine.arena

import net.minestom.server.coordinate.Vec
import org.readutf.game.engine.arena.marker.Marker

data class ArenaTemplate(
    val name: String,
    val positions: Map<String, Marker>,
    val size: Vec,
    val supportedGames: List<String>,
)
