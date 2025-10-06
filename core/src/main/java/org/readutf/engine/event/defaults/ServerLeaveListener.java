package org.readutf.engine.event.defaults;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.impl.game.GameServerLeaveEvent;

import java.util.UUID;

public class ServerLeaveListener {

    public void onServerLeave(UUID playerId) {
        Game<?, ?, ?> game = GameManager.getInstance().getGameByPlayer(playerId);

        if(game != null) {
            game.callEvent(new GameServerLeaveEvent(game));
        }
    }

}
