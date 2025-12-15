package org.readutf.engine.arena;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.BuildFormatManager;
import org.readutf.buildformat.BuildManager;
import org.readutf.buildformat.BuildMeta;
import org.readutf.buildformat.store.BuildMetaStore;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the loading and retrieval of arenas within the game engine.
 * <p>
 * Responsible for:
 * <ul>
 *   <li>Loading arenas by name and build format, assigning unique IDs, and placing them in the world.</li>
 *   <li>Retrieving lists of build names that match specific formats and checksums.</li>
 * </ul>
 *
 * @param <WORLD> the type representing the world/environment where arenas are placed
 */
public class ArenaManager<WORLD> {

    private static final Logger logger = LoggerFactory.getLogger(ArenaManager.class);
    private static final BuildFormatManager buildFormatManager = BuildFormatManager.getInstance();

    private @NotNull
    final BuildMetaStore buildMetaStore;
    private @NotNull
    final ArenaPlatform<WORLD> arenaPlatform;
    private @NotNull
    final AtomicInteger idTracker;

    public ArenaManager(@NotNull ArenaPlatform<WORLD> arenaPlatform, @NotNull BuildMetaStore buildMetaStore) {
        this.arenaPlatform = arenaPlatform;
        this.buildMetaStore = buildMetaStore;
        this.idTracker = new AtomicInteger(0);
    }

    /**
     * Loads an arena by name and build format class.
     * Retrieves the build, assigns a unique ID, places it in the world,
     * constructs the format, and returns a new Arena instance.
     *
     * @param name     the name of the build to load
     * @param clazz    the class of the build format
     * @param <FORMAT> the type of the build format
     * @return a new Arena instance or null if the build is not found
     * @throws ArenaLoadException if loading fails or the build format is invalid
     */
    public @Nullable <FORMAT> Arena<WORLD, FORMAT> loadArena(@NotNull BuildMeta meta, @NotNull Class<FORMAT> clazz) throws ArenaLoadException {
        try {

            int id = idTracker.getAndIncrement();
            BuildPlacement<WORLD> placement = arenaPlatform.placeBuild(id, meta);

            FORMAT format = buildFormatManager.construct(clazz, meta.metadata().requirements());

            return new Arena<>(
                    id,
                    meta,
                    placement.world(),
                    format
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a list of build names that match the specified format and class.
     *
     * @param format the format identifier to filter builds
     * @param kClass the class of the build format to validate against
     * @param <T>    the type of the build format
     * @return a list of build names matching the format and checksum
     * @throws Exception if validation or checksum generation fails
     */
    @Blocking
    public <T> @NotNull List<BuildMeta> getByFormat(@NotNull String format, @NotNull Class<T> kClass) throws Exception {
        Map<String, Integer> buildsByFormat = buildMetaStore.getBuildsByFormat(format);

        int targetChecksum = buildFormatManager.checksum(buildFormatManager.generateRequirements(kClass));

        List<String> builds = new ArrayList<>();

        List<BuildMeta> buildMetas = new ArrayList<>(buildsByFormat.size());
        for (Map.Entry<String, Integer> stringIntegerEntry : buildsByFormat.entrySet()) {
            BuildMeta meta = buildMetaStore.getBuild(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
            if (meta == null) continue;
            if (meta.checksum() == targetChecksum) {
                buildMetas.add(meta);
            }
        }

        return buildMetas;
    }


}
