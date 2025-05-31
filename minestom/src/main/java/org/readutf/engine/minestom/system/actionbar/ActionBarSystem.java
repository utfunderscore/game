package org.readutf.engine.minestom.system.actionbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.readutf.engine.Game;
import org.readutf.engine.feature.System;
import org.readutf.engine.minestom.MinestomPlatform;
import org.readutf.engine.task.GameTask;
import org.readutf.engine.task.impl.RepeatingGameTask;

public class ActionBarSystem implements System {
    private final Game<?, ?, ?> genericGame;
    private final ActionBarProvider textProvider;

    public ActionBarSystem(Game<?, ?, ?> genericGame, ActionBarProvider textProvider) {
        this.genericGame = genericGame;
        this.textProvider = textProvider;
    }

    @Override
    public List<GameTask> getTasks() {
        return List.of(new ActionBarTask());
    }

    private class ActionBarTask extends RepeatingGameTask {
        private final Map<Player, Component> previousText = new HashMap<>();

        public ActionBarTask() {
            super(0, 1);
        }

        @Override
        public void run() {
            for (Player player : MinestomPlatform.getOnlinePlayers(genericGame)) {
                Component newLine = textProvider.getText(player);
                Component previousLine = previousText.getOrDefault(player, Component.empty());

                if (!newLine.equals(previousLine)) {
                    player.sendActionBar(newLine);
                    previousText.put(player, newLine);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        // No shutdown logic provided in original
    }
}