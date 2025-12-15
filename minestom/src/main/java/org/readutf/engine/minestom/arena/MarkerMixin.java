package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.readutf.buildformat.types.Position;

public abstract class MarkerMixin {

    @JsonIgnore
    public abstract Position getTargetPosition();

}
