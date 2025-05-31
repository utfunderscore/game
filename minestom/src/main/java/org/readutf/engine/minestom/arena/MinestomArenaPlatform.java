package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.hollowcube.schem.BlockEntityData;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SpongeSchematic;
import net.hollowcube.schem.reader.SchematicReadException;
import net.hollowcube.schem.reader.SpongeSchematicReader;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.exception.BuildFormatException;
import org.readutf.buildformat.common.markers.Marker;
import org.readutf.buildformat.common.markers.Position;
import org.readutf.buildformat.common.meta.BuildMeta;
import org.readutf.buildformat.common.schematic.BuildSchematic;
import org.readutf.buildformat.common.schematic.BuildSchematicStore;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.arena.ArenaPlatform;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;
import org.readutf.engine.minestom.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinestomArenaPlatform implements ArenaPlatform<Instance> {

    private static final Logger logger = LoggerFactory.getLogger(MinestomArenaPlatform.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private @NotNull final BuildSchematicStore buildSchematicStore;
    private @NotNull final SpongeSchematicReader schematicReader;
    private final @NotNull File cacheDirectory;
    private final Map<String, CachedArenaData> arenaCache;

    public MinestomArenaPlatform(@NotNull BuildSchematicStore buildSchematicStore, @NotNull File cacheDirector) {
        this.buildSchematicStore = buildSchematicStore;
        this.schematicReader = new SpongeSchematicReader();
        this.cacheDirectory = cacheDirector;
        this.arenaCache = new HashMap<>();
        MAPPER.addMixIn(Marker.class, MarkerMixin.class);
        for (File file : Optional.ofNullable(cacheDirectory.listFiles()).orElse(new File[0])) {
            try {
                arenaCache.put(getNameWithoutExtension(file), loadCachedArena(file));
            } catch (IOException e) {
                logger.warn("Failed to load cached arena data", e);
            }
        }
    }

    @Override
    public @NotNull BuildPlacement<Instance> placeBuild(int buildId, @NotNull BuildMeta buildMeta) throws ArenaLoadException {

        @Nullable CachedArenaData cachedArenaData;
        synchronized (arenaCache) {
            cachedArenaData = arenaCache.get(buildMeta.name());
        }

        if (cachedArenaData == null || cachedArenaData.version() != buildMeta.version()) {
            cachedArenaData = retrieveArenaData(buildMeta);
        }

        PolarWorld read = PolarReader.read(cachedArenaData.polarData());
        PolarLoader polarLoader = new PolarLoader(read);

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(polarLoader);
        instance.setChunkSupplier(LightingChunk::new);

        return new BuildPlacement<>(instance, Position.ZERO, cachedArenaData.markers());
    }

    private @NotNull CachedArenaData retrieveArenaData(@NotNull BuildMeta buildMeta) throws ArenaLoadException {
        logger.info("Download build data for {}", buildMeta.name());
        long start = System.currentTimeMillis();
        @NotNull BuildSchematic buildSchematic;
        try {
            buildSchematic = buildSchematicStore.load(buildMeta.name());
        } catch (BuildFormatException e) {
            throw new ArenaLoadException("Failed to load build data", e);
        }
        logger.info("Build data loaded in {} ms", System.currentTimeMillis() - start);
        CachedArenaData cachedArenaData = getArenaData(buildMeta, buildSchematic);
        synchronized (arenaCache) {
            arenaCache.put(buildMeta.name(), cachedArenaData);
        }
        try {
            storeCachedArena(buildMeta.name(), cachedArenaData);
        } catch (IOException e) {
            logger.warn("Failed to store build data for {}", buildMeta.name(), e);
        }
        return cachedArenaData;
    }

    private void storeCachedArena(String name, CachedArenaData cachedArenaData) throws IOException {
        MAPPER.writeValue(new File(cacheDirectory, name + ".json"), cachedArenaData);
    }

    private @NotNull CachedArenaData loadCachedArena(File file) throws IOException {
        return MAPPER.readValue(file, CachedArenaData.class);
    }

    @Override
    public void freeArena(Arena<Instance, ?> arena) {
        MinecraftServer.getInstanceManager().unregisterInstance(arena.getWorld());
    }

    public String getNameWithoutExtension(@NotNull File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private @NotNull CachedArenaData getArenaData(@NotNull BuildMeta buildMeta, @NotNull BuildSchematic buildData)
            throws ArenaLoadException {

        SpongeSchematic schematic;
        try {
            schematic = (SpongeSchematic) schematicReader.read(buildData.buildData());
        } catch (SchematicReadException e) {
            throw new ArenaLoadException("Failed to read schematic", e);
        }

        List<Marker> markers = extractMarkerPositions(schematic).stream()
                .map(marker -> new Marker(
                        marker.name(),
                        marker.position().add(PlatformUtils.fromPoint(schematic.offset())),
                        marker.offset()))
                .toList();

        var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);
        PolarWorld polarWorld = new PolarWorld();
        PolarLoader chunkLoader = new PolarLoader(polarWorld);
        instance.setChunkLoader(chunkLoader);
        Point size = schematic.size();

        @NotNull Point min = new Pos(0, 0, 0);
        @NotNull Point max = min.add(size.blockX(), size.blockY(), size.blockZ());

        int minChunkX = min.blockX() >> 4;
        int minChunkZ = min.blockZ() >> 4;

        int maxChunkX = max.blockX() >> 4;
        int maxChunkZ = max.blockZ() >> 4;

        List<Chunk> chunks = new ArrayList<>();
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Chunk chunk = instance.loadChunk(x, z).join();
                chunks.add(chunk);
            }
        }

        LightingChunk.relight(instance, chunks);
        CompletableFuture<Void> pasteJob = new CompletableFuture<>();
        schematic.createBatch(block -> {
                    if (block == null) return null;
                    CompoundBinaryTag nbt = block.nbt();
                    if(nbt == null) return block;
                    System.out.println(nbt);
                    boolean frontText = nbt.keySet().contains("front_text");
                    if (!frontText) return block;

                    return Block.AIR;
                })
                .apply(instance, schematic.offset().mul(-1), () -> {
                    pasteJob.complete(null);
                });
        pasteJob.join();

        instance.saveChunksToStorage().join();

        PolarWriter.write(polarWorld);

        byte[] polarData = PolarWriter.write(polarWorld);
        MinecraftServer.getInstanceManager().unregisterInstance(instance);

        return new CachedArenaData(buildMeta.version(), polarData, markers);
    }

    private record CachedArenaData(int version, byte[] polarData, List<Marker> markers) {}

    private @NotNull List<Marker> extractMarkerPositions(@NotNull Schematic schematic) {
        List<Marker> markers = new ArrayList<>();

        for (BlockEntityData entity : schematic.blockEntities()) {
            CompoundBinaryTag data = entity.data();
            if (!"minecraft:sign".equals(data.getString("id"))) continue;

            List<String> markerLines = extractMarkerLines(data);
            Point point = entity.position().sub(schematic.offset());
            String key = point.toString();

            if (markerLines.isEmpty() || !"#marker".equalsIgnoreCase(markerLines.get(0))) {
                // logger.debug("Sign at does not start with #marker lines: " + markerLines);
                continue;
            }

            // logger.info("Found marker at " + key);

            if (markerLines.size() < 2) continue;
            String markerName = markerLines.get(1);
            if (markerName.isEmpty()) {
                continue;
            }

            List<Integer> offset = List.of(0, 0, 0);
            if (markerLines.size() > 2 && !markerLines.get(2).isEmpty()) {
                offset = Stream.of(markerLines.get(2).split("[,\\- ]"))
                        .map(s -> {
                            try {
                                return Integer.parseInt(s);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();
            }
            if (offset.size() != 3) {
                // logger.info("Marker at " + key + " does not have a valid offset");
                continue;
            }

            markers.add(new Marker(
                    markerName,
                    new Position(point.x() + offset.get(0), point.y() + offset.get(1), point.z() + offset.get(2)),
                    new Position(offset.get(0), offset.get(1), offset.get(2))));
        }

        return markers;
    }

    private List<String> extractMarkerLines(CompoundBinaryTag compoundBinaryTag) {
        CompoundBinaryTag frontText = compoundBinaryTag.getCompound("front_text");
        ListBinaryTag messages = frontText.getList("messages");
        List<String> result = new ArrayList<>(messages.size());
        for (BinaryTag tag : messages) {
            if (tag instanceof StringBinaryTag line) {
                String value = line.value();
                if (value.length() < 2) continue;
                logger.debug("Extracted marker line: {}", value);
                result.add(value);
            }
        }
        return result;
    }
}