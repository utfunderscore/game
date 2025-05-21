package org.readutf.engine.arena;

import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.Build;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;

/**
 * Represents a platform for managing arenas in a specific world/environment.
 * <p>
 * This interface provides methods for placing builds into a world and freeing arenas.
 * It is designed to be implemented for specific world types.
 *
 * @param <WORLD> the type representing the world/environment where arenas are managed
 */
public interface ArenaPlatform<WORLD> {

    /**
     * Places the build into a world, and returns the origin
     */
    @NotNull
    BuildPlacement<WORLD> placeBuild(int buildId, @NotNull Build build) throws ArenaLoadException;

    /**
     * Frees an arena.
     */
    void freeArena(Arena<WORLD, ?> arena) throws ArenaLoadException;
}
