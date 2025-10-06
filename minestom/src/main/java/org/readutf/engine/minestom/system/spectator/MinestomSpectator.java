package org.readutf.engine.minestom.system.spectator;

import io.github.togar2.pvp.events.FinalAttackEvent;
import io.github.togar2.pvp.events.FinalDamageEvent;
import java.util.List;
import java.util.UUID;
import net.minestom.server.entity.Player;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.feature.spectator.SpectatorData;
import org.readutf.engine.feature.spectator.SpectatorPlatform;
import org.readutf.engine.feature.spectator.SpectatorSystem;
import org.readutf.engine.minestom.MinestomPlatform;
import org.readutf.engine.minestom.PlatformUtils;

public class MinestomSpectator implements SpectatorPlatform {

    @Override
    public void setSpectatorState(SpectatorData spectatorData) {
        Player player = MinestomPlatform.getPlayer(spectatorData.getPlayerId());
        if (player == null) return;

        player.teleport(PlatformUtils.toPos(spectatorData.getSpectatorPosition()));

        player.setAllowFlying(true);
        player.setFlying(true);
        player.updateViewerRule();
    }

    @Override
    public void setNormalState(UUID playerId) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if (player == null) return;

        player.setAllowFlying(false);
        player.setFlying(false);
        player.updateViewerRule();
    }

    @Override
    public List<ListenerData> getListeners(SpectatorSystem spectatorSystem) {
        SpectatorCombatListeners combatListeners = new SpectatorCombatListeners(spectatorSystem);
        return List.of(
                ListenerData.typed(FinalAttackEvent.class, combatListeners.combatPreventionListener()),
                ListenerData.typed(FinalDamageEvent.class, combatListeners.damagePreventionListener())
        );
    }
}
