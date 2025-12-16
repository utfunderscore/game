package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.BuildData;
import org.readutf.buildformat.BuildMeta;
import org.readutf.buildformat.store.BuildDataStore;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.arena.ArenaPlatform;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

public class MinestomArenaPlatform implements ArenaPlatform<Instance> {

    private static final Logger logger = LoggerFactory.getLogger(MinestomArenaPlatform.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private @NotNull
    final BuildDataStore buildDataStore;

    public MinestomArenaPlatform(@NotNull BuildDataStore buildDataStore, @NotNull File cacheDirector) {
        this.buildDataStore = buildDataStore;
    }

    @Override
    public @NotNull BuildPlacement<Instance> placeBuild(int buildId, @NotNull BuildMeta buildMeta) throws ArenaLoadException, Exception {

        BuildData buildData = buildDataStore.get(buildMeta.name(), buildMeta.version());
        if (buildData == null) throw new ArenaLoadException("Build not found: " + buildMeta.name());

        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(
                DimensionType.OVERWORLD,
                new PolarLoader(new ByteArrayInputStream(buildData.polarData())));

        return new BuildPlacement<>(instance);
    }

    @Override
    public void freeArena(Arena<Instance, ?> arena) {
        MinecraftServer.getInstanceManager().unregisterInstance(arena.getWorld());
    }


}