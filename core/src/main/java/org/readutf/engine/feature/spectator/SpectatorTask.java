package org.readutf.engine.feature.spectator;

import java.time.LocalDateTime;
import java.util.UUID;
import org.readutf.engine.task.impl.RepeatingGameTask;

public class SpectatorTask extends RepeatingGameTask {
    private final SpectatorSystem spectatorManager;

    public SpectatorTask(SpectatorSystem spectatorManager) {
        super(0, 50);
        this.spectatorManager = spectatorManager;
    }

    @Override
    public void run() {
        for (UUID onlineSpectator : spectatorManager.getOnlineSpectators()) {
            SpectatorData spectatorData = spectatorManager.getSpectatorData(onlineSpectator);
            if (spectatorData == null) continue;
            if (!spectatorData.isCanRespawn()) return;

            if (spectatorData.getRespawnTime().isBefore(LocalDateTime.now())) {
                spectatorManager.respawnSpectator(spectatorData);
            }
        }
    }
}