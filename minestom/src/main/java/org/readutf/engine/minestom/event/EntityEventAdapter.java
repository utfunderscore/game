package org.readutf.engine.minestom.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.adapter.TypedEventAdapter;

public class EntityEventAdapter implements TypedEventAdapter<EntityEvent> {

    @NotNull
    private final GameManager gameManager;

    public EntityEventAdapter(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public @Nullable Game<?, ?, ?> convertEvent(EntityEvent event) {

        var entity = event.getEntity();

        if (entity instanceof Player player) {
            return gameManager.getGameByPlayer(player.getUuid());
        }

        var instance = entity.getInstance();

        for (Game<?, ?, ?> game : gameManager.getGames()) {
            Arena<?, ?> arena = game.getArena();
            if (arena != null && arena.getWorld() == instance) {
                return game;
            }
        }
        return null;
    }
}
