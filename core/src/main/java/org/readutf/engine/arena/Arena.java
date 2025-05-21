package org.readutf.engine.arena;

import java.util.List;
import java.util.UUID;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.markers.Position;

public record Arena<WORLD, T extends BuildFormat>(
        int arenaId,
        Build build,
        WORLD world,
        BuildFormat format,
        List<Position> positions
) {
}
