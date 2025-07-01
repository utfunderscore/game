package org.readutf.engine.minestom.system.visibility;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.feature.visibility.VisibilityHandler;
import org.readutf.engine.feature.visibility.VisibilityPlatform;
import org.readutf.engine.minestom.MinestomPlatform;

import java.util.UUID;

public class MinestomVisibilityPlatform implements VisibilityPlatform {

    public void setPlayerVisibility(@NotNull UUID playerId, VisibilityHandler visibilityHandler) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if (player == null) return;

        player.updateViewerRule(entity -> {
            if (!(entity instanceof Player target)) {
                return true; // Always show non-player entities
            }

            return visibilityHandler.isVisibleToPlayer(player.getUuid(), target.getUuid());
        });
    }

    @Override
    public void refreshVisibility(@NotNull UUID playerId) {
        Player player = MinestomPlatform.getPlayer(playerId);
        if (player == null) return;

        player.updateViewerRule();
    }
}
