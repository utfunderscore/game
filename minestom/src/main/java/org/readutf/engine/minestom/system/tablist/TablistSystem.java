package org.readutf.engine.minestom.system.tablist;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.feature.System;
import org.readutf.engine.feature.visibility.VisibilityHandler;
import org.readutf.engine.minestom.MinestomPlatform;

public class TablistSystem implements System {

    private final @NotNull Game<?, ?, ?> game;

    @NotNull
    private final VisibilityHandler visibilityHandler;

    public TablistSystem(@NotNull Game<?, ?, ?> game, @NotNull VisibilityHandler visibilityHandler) {
        this.game = game;
        this.visibilityHandler = visibilityHandler;
    }

    public void refresh() {
        for (Player onlinePlayer : MinestomPlatform.getOnlinePlayers(game)) {
            TablistManager.getInstance().updateTablist(onlinePlayer);
        }
    }

    public @NotNull VisibilityHandler getVisibilityHandler() {
        return visibilityHandler;
    }
}
