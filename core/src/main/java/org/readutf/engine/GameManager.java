package org.readutf.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.impl.game.GameCrashEvent;
import org.readutf.engine.event.impl.game.GameEndEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class GameManager {

    private @NotNull final HashSet<Game<?, ?, ?>> gameTracker = new HashSet<>();

    public GameManager() {
    }

    public void start(@NotNull Game<?, ?, ?> game) throws GameException {
        game.start();
        gameTracker.add(game);
    }

    public void unregister(@NotNull Game<?, ?, ?> game) throws EventDispatchException {
        game.registerListener(GameEndEvent.class,event -> {
            gameTracker.remove(game);
        });
        game.registerListener(GameCrashEvent.class, event -> {
            gameTracker.remove(game);
        });
    }

    public @Nullable Game<?, ?, ?> getGameByPlayer(UUID uuid) {
        for (Game<?, ?, ?> game : gameTracker) {
            if (game.getPlayers().contains(uuid)) {
                return game;
            }
        }
        return null;
    }

    public @NotNull Collection<Game<?, ?, ?>> getGames() {
        return gameTracker;
    }
}
