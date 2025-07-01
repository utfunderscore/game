package org.readutf.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class GameManager {

    private @NotNull static final HashSet<Game<?, ?, ?>> gameTracker = new HashSet<>();

    public static void register(@NotNull Game<?, ?, ?> game) {
        gameTracker.add(game);
    }

    public static void unregister(Game<?, ?, ?> game) {
        gameTracker.remove(game);
    }

    public static @Nullable Game<?, ?, ?> getGameByPlayer(UUID uuid) {
        for (Game<?, ?, ?> game : gameTracker) {
            if (game.getPlayers().contains(uuid)) {
                return game;
            }
        }
        return null;
    }


    public static @NotNull Collection<Game<?, ?, ?>> getGames() {
        return gameTracker;
    }
}
