package org.readutf.engine.feature.spectator;

import java.util.List;
import java.util.UUID;
import org.readutf.engine.event.listener.ListenerData;

public interface SpectatorPlatform {

    void setSpectatorState(SpectatorData spectatorData);

    void setNormalState(UUID playerId) throws Exception;

    List<ListenerData> getListeners(SpectatorSystem spectatorSystem);

}
