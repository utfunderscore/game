package org.readutf.engine.minestom.system.spectator;

import io.github.togar2.pvp.events.FinalAttackEvent;
import io.github.togar2.pvp.events.FinalDamageEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.spectator.SpectatorSystem;

public class SpectatorCombatListeners {

    private @NotNull final SpectatorSystem spectatorSystem;

    public SpectatorCombatListeners(@NotNull SpectatorSystem spectatorSystem) {
        this.spectatorSystem = spectatorSystem;
    }

    public TypedGameListener<FinalAttackEvent> combatPreventionListener() {
        return event -> {
            if (event.getEntity() instanceof Player player && spectatorSystem.isSpectator(player.getUuid())) {
                event.setCancelled(true);
            }
        };
    }

    public TypedGameListener<FinalDamageEvent> damagePreventionListener() {
        return event -> {
            if(event.getEntity() instanceof Player player && spectatorSystem.isSpectator(player.getUuid())) {
                event.setCancelled(true);
            }
        };
    }
}
