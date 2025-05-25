package org.readutf.engine.feature.spectator;

import java.util.UUID;

public interface SpectatorPlatform {

    void setSpectatorState(SpectatorData spectatorData);

    void setNormalState(UUID playerId) throws Exception;

}
