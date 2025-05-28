package org.readutf.engine.arena.build;

import java.util.List;
import org.readutf.buildformat.common.markers.Marker;
import org.readutf.buildformat.common.markers.Position;

public record BuildPlacement<WORLD>(WORLD world, Position origin, List<Marker> markers) {}
