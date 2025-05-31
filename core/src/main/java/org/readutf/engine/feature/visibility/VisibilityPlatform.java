package org.readutf.engine.feature.visibility;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface VisibilityPlatform {

    void setPlayerVisibility(@NotNull UUID playerId, VisibilityHandler visibilityHandler);

    void refreshVisibility(@NotNull UUID playerId);

}
