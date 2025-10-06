package org.readutf.engine.minestom.arena;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jetbrains.annotations.Unmodifiable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
        MAPPER.addMixIn(Position.class, PositionMixin.class);
        if(!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
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

        if (cachedArenaData == null || cachedArenaData.meta().version() != buildMeta.version()) {
            cachedArenaData = retrieveArenaData(buildMeta);
        }

        PolarWorld read = PolarReader.read(cachedArenaData.polarData());
        PolarLoader polarLoader = new PolarLoader(read);

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(polarLoader);
        instance.setChunkSupplier(LightingChunk::new);

        return new BuildPlacement<>(instance, Position.ZERO, cachedArenaData.meta().markers());
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
        logger.debug("Storing cached arena data for {}", name);
        long start = System.currentTimeMillis();

        File directory = new File(cacheDirectory, name);
        if(!directory.exists()) directory.mkdirs();
        File metaFile = new File(directory, "meta.json");
        File polarFile = new File(directory, "build.polar");

        metaFile.createNewFile();
        polarFile.createNewFile();

        MAPPER.writeValue(metaFile, cachedArenaData.meta());
        Files.write(polarFile.toPath(), cachedArenaData.polarData());
        logger.debug("Cached arena data for {} stored successfully in {}ms", name, System.currentTimeMillis() - start);
    }

    private @NotNull CachedArenaData loadCachedArena(File directory) throws IOException {
        File metaFile = new File(directory, "meta.json");
        File polarFile = new File(directory, "build.polar");

        if (!metaFile.exists() || !polarFile.exists()) {
            throw new IOException("Cached arena data is incomplete: " + directory.getName());
        }

        ArenaMeta metaData = MAPPER.readValue(metaFile, ArenaMeta.class);
        byte[] polarData = Files.readAllBytes(polarFile.toPath());

        return new CachedArenaData(metaData, polarData);
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

        List<Marker> markers = extractMarkers(schematic);

        System.out.println(markers);

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
        schematic.createBatch((point, block) -> {
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

        return new CachedArenaData(new ArenaMeta(buildMeta.version(), markers), polarData);
    }

    private @NotNull @Unmodifiable List<Marker> extractMarkers(@NotNull Schematic schematic) {

        HashMap<Point, Marker> markers = new HashMap<>();

        for (BlockEntityData entity : schematic.blockEntities()) {
            CompoundBinaryTag data = entity.data();
            String id = data.getString("id");
            if (!"minecraft:sign".equals(id)) continue;

            List<String> markerLines = extractMarkerLines(data);
            Point point = entity.position();

            if (markerLines.isEmpty() || !markerLines.get(0).equalsIgnoreCase("#marker")) {
                continue;
            }

            if (markerLines.size() < 2) continue;
            String markerName = markerLines.get(1);
            if (markerName.isEmpty()) continue;

            int[] offset = new int[]{0, 0, 0};
            if (markerLines.size() > 2 && !markerLines.get(2).isEmpty()) {
                String[] parts = markerLines.get(2).split("[,\\- ]");
                List<Integer> offsets = new ArrayList<>();
                for (String part : parts) {
                    try {
                        offsets.add(Integer.parseInt(part));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (offsets.size() == 3) {
                    offset = new int[]{offsets.get(0), offsets.get(1), offsets.get(2)};
                } else {
                    continue;
                }
            }

            markers.put(point, new Marker(markerName,
                    new Position(point.x(), point.y(), point.z()),
                    new Position(offset[0], offset[1], offset[2])
            ));
        }


        System.out.println(markers.keySet());
        schematic.forEachBlock((temp, block) -> {
            Point point = temp.sub(schematic.offset());

            for (Point point1 : markers.keySet()) {
                if (point1.x() == point.x() &&
                        point1.y() == point.y() &&
                        point1.z() == point.z()) {
                    System.out.println(block.properties());
                    double yaw = Integer.parseInt(block.properties().get("rotation")) * 22.5;
                    Marker marker = markers.get(point1);
                    Position targetPosition = marker.origin();
                    Position position = new Position(targetPosition.x(), targetPosition.y(), targetPosition.z(), (float) yaw, 0);
                    markers.put(point1, new Marker(marker.name(), position, marker.offset()));
                }
            }
        });

        return markers.values().stream().toList();
    }

    private @NotNull List<String> extractMarkerLines(@NotNull CompoundBinaryTag compoundBinaryTag) {

        CompoundBinaryTag frontText = compoundBinaryTag.getCompound("front_text");
        ListBinaryTag messages = frontText.getList("messages");

        List<String> result = new ArrayList<>();
        for (BinaryTag tag : messages) {
            if (tag instanceof StringBinaryTag stringTag) {
                String value = stringTag.value();
                if (value.length() < 2) continue;
                // logger.debug("Extracted marker line: {}", value);
                result.add(value);
            }
        }
        return result;
    }

    private record CachedArenaData(ArenaMeta meta, byte[] polarData) {}

    public record ArenaMeta(int version, List<Marker> markers) {}

}