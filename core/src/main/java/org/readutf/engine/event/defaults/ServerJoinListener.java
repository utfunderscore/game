package org.readutf.engine.event.defaults;

import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.impl.game.GameServerJoinEvent;

import java.util.UUID;

public class ServerJoinListener {

    public void onJoin(UUID playerId) {
        Game<?, ?, ?> game = GameManager.getInstance().getGameByPlayer(playerId);

        if(game != null) {
            game.callEvent(new GameServerJoinEvent(game));
        }
    }

}
