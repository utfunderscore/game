package org.readutf.game.engine.utils

import net.minestom.server.coordinate.Pos

fun pos(
    x: Int,
    y: Int,
    z: Int,
): Pos = Pos(x.toDouble(), y.toDouble(), z.toDouble())
