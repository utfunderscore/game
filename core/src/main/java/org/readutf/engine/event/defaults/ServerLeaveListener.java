package org.readutf.engine.event.defaults;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.impl.game.GameServerLeaveEvent;

import java.util.UUID;

public class ServerLeaveListener {

    @NotNull
    private final GameManager gameManager;

    public ServerLeaveListener(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void onServerLeave(UUID playerId) {
        Game<?, ?, ?> game = gameManager.getGameByPlayer(playerId);

        if(game != null) {
            game.callEvent(new GameServerLeaveEvent(game));
        }
    }

}
