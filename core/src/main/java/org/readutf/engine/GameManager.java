package org.readutf.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
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

    public static @Nullable Game<?, ?, ?> getGameByName(String name) {
        return gameTracker.stream()
                .filter(game -> game.getEasyId().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static @NotNull List<String> getGameNames() {
        return gameTracker.stream()
                .map(Game::getEasyId)
                .toList();
    }

    public static @NotNull Collection<Game<?, ?, ?>> getGames() {
        return gameTracker;
    }
}
