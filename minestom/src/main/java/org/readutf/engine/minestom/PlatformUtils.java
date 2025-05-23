package org.readutf.engine.minestom;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.arena.Arena;

public class PlatformUtils {

    public static @Nullable Game<?, ?, ?> getGame(Instance instance) {
        for (Game<?, ?, ?> game : GameManager.getGames()) {
            Arena<?, ?> arena = game.getArena();
            if(arena != null && arena.getWorld() instanceof Instance targetInstance && targetInstance == instance) {
                return game;
            }
        }
        return null;
    }

}
