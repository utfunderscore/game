package org.readutf.engine.feature.spectator;

import java.time.LocalDateTime;
import java.util.UUID;
import org.readutf.engine.task.impl.RepeatingGameTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectatorTask extends RepeatingGameTask {
    private static final Logger log = LoggerFactory.getLogger(SpectatorTask.class);
    private final SpectatorSystem spectatorManager;

    public SpectatorTask(SpectatorSystem spectatorManager) {
        super(0, 50);
        this.spectatorManager = spectatorManager;
    }

    @Override
    public void run() {
        for (UUID onlineSpectator : spectatorManager.getOnlineSpectators()) {
            SpectatorData spectatorData = spectatorManager.getSpectatorData(onlineSpectator);
            if (spectatorData == null || spectatorData.getRespawnTime() == null) continue;
            if (!spectatorData.isCanRespawn()) return;

            if (spectatorData.getRespawnTime().isBefore(LocalDateTime.now())) {
                try {
                    spectatorManager.respawnSpectator(spectatorData);
                } catch (Exception e) {
                    log.warn("Failed to respawn spectator {}", onlineSpectator, e);
                    spectatorManager.setSpectator(SpectatorData.permanent(spectatorData.getPlayerId(), LocalDateTime.now(), spectatorData.getSpectatorPosition()));
                }
            }
        }
    }
}