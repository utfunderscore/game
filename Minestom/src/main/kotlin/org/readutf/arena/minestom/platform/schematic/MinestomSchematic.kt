package org.readutf.arena.minestom.platform.schematic

import net.hollowcube.schem.Schematic
import org.readutf.game.engine.platform.schematic.ArenaSchematic
import org.readutf.game.engine.utils.BlockPosition

class MinestomSchematic(
    val schematic: Schematic,
) : ArenaSchematic {

    override fun getSize(): BlockPosition {
        val size = schematic.size()
        return BlockPosition(size.x(), size.y(), size.z())
    }
}
