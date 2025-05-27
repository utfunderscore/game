package org.readutf.engine.minestom.feature.spectator;

import io.github.togar2.pvp.events.FinalAttackEvent;
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

        player.teleport(PlatformUtils.fromPosition(spectatorData.getSpectatorPosition()));

        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setAutoViewable(false);
    }

    @Override
    public void setNormalState(UUID playerId) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if (player == null) return;

        player.setAllowFlying(false);
        player.setFlying(false);
        player.setInvisible(false);
    }

    @Override
    public List<ListenerData> getListeners(SpectatorSystem spectatorSystem) {
        SpectatorCombatListeners combatListeners = new SpectatorCombatListeners(spectatorSystem);
        return List.of(ListenerData.typed(FinalAttackEvent.class, combatListeners.getDamageListener()));
    }
}
