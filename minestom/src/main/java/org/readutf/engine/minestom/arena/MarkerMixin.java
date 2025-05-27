package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.readutf.buildformat.common.markers.Position;

public abstract class MarkerMixin {

    @JsonIgnore
    public abstract Position getTargetPosition();

}
