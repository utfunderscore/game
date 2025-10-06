package org.readutf.engine.event.defaults;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.GamePlatform;
import org.readutf.engine.event.impl.game.GameServerJoinEvent;

import java.util.UUID;

public class ServerJoinListener {

    @NotNull
    private final GameManager gameManager;

    public ServerJoinListener(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void onJoin(UUID playerId) {
        Game<?, ?, ?> game = gameManager.getGameByPlayer(playerId);

        if(game != null) {
            game.callEvent(new GameServerJoinEvent(game));
        }
    }

}
