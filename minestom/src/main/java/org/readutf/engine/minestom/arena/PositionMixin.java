package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.readutf.buildformat.common.markers.Position;

public abstract class PositionMixin {

    @JsonIgnore
    public abstract Position getBlockX();

    @JsonIgnore
    public abstract Position getBlockZ();

    @JsonIgnore
    public abstract Position getBlockY();

}
