package org.readutf.engine.minestom.event;

import net.minestom.server.event.trait.InstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.adapter.TypedEventAdapter;

public class InstanceEventAdapter implements TypedEventAdapter<InstanceEvent> {

    @NotNull
    private final GameManager gameManager;

    public InstanceEventAdapter(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public @Nullable Game<?, ?, ?> convertEvent(InstanceEvent event) {
        for (Game<?, ?, ?> game : gameManager.getGames()) {
            Arena<?, ?> arena = game.getArena();
            if (arena != null && arena.getWorld() == event.getInstance()) {
                return game;
            }
        }
        return null;
    }
}
