package org.readutf.engine.feature.visibility;

import java.util.UUID;

@FunctionalInterface
public interface VisibilityHandler {

    boolean isVisibleToPlayer(UUID viewer, UUID target);

}
