package org.readutf.game.engine.arena

import org.readutf.buildformat.common.markers.Marker
import org.readutf.buildformat.common.markers.Position
import org.readutf.game.engine.world.GameWorld

/**
 * Represents the resulting data from a build placement
 * Used in the [ArenaManager] to track the builds origin and markers found when placed
 */
data class BuildPlacement(
    val world: GameWorld,
    val origin: Position,
    val markers: List<Marker>,
)
