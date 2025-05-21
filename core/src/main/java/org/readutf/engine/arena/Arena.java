package org.readutf.engine.arena;

import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.markers.Position;

import java.util.List;

public record Arena<WORLD, T extends BuildFormat>(
        int arenaId,
        Build build,
        WORLD world,
        BuildFormat format,
        List<Position> positions
) {
}
