package org.readutf.engine.minestom.feature.spectator;

import io.github.togar2.pvp.events.FinalAttackEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.spectator.SpectatorSystem;

public class SpectatorCombatListeners {

    private @NotNull final SpectatorSystem spectatorSystem;

    public SpectatorCombatListeners(@NotNull SpectatorSystem spectatorSystem) {
        this.spectatorSystem = spectatorSystem;
    }

    public TypedGameListener<FinalAttackEvent> getDamageListener() {
        return event -> {
            if (event.getEntity() instanceof Player player && spectatorSystem.isSpectator(player.getUuid())) {
                event.setCancelled(true);
            }
        };
    }
}
