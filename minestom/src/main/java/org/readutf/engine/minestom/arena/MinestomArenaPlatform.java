package org.readutf.engine.minestom.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.hollowcube.schem.BlockEntityData;
import net.hollowcube.schem.Schematic;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.markers.Marker;
import org.readutf.buildformat.common.markers.Position;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.arena.ArenaPlatform;
import org.readutf.engine.arena.build.BuildPlacement;
import org.readutf.engine.arena.exception.ArenaLoadException;
import org.readutf.engine.minestom.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MinestomArenaPlatform implements ArenaPlatform<Instance> {

    private static final Logger logger = LoggerFactory.getLogger(MinestomArenaPlatform.class);
    private @NotNull static final MinestomArenaPlatform instance = new MinestomArenaPlatform();

    private MinestomArenaPlatform() {
        // Private constructor to prevent instantiation
    }

    private @NotNull static final SpongeSchematicReader SCHEMATIC_READER = new SpongeSchematicReader();
    private @NotNull final Map<Integer, Instance> instances = new HashMap<>();

    @Override
    public @NotNull BuildPlacement<Instance> placeBuild(int buildId, @NotNull Build build) throws ArenaLoadException {
        var instance = MinecraftServer.getInstanceManager().createInstanceContainer();

        Schematic schematic;
        try {
            schematic = SCHEMATIC_READER.read(build.buildSchematic().buildData());
        } catch (SchematicReadException e) {
            throw new ArenaLoadException("Failed to read schematic", e);
        }

        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        Point size = schematic.size();

        @NotNull Point min = schematic.offset();
        @NotNull Point max = min.add(size.blockX(), size.blockY(), size.blockZ());

        int minChunkX = min.blockX() >> 4;
        int minChunkZ = min.blockZ() >> 4;

        int maxChunkX = max.blockX() >> 4;
        int maxChunkZ = max.blockZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                CompletableFuture<Chunk> chunkFuture = instance.loadChunk(x, z);
                chunkFutures.add(chunkFuture);
            }
        }

        logger.info("Offset {}" , schematic.offset());

        CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).thenAccept(x -> schematic.createBatch().apply(instance, Pos.ZERO, () -> {}));

        List<Marker> markers;
        try {
            markers = extractMarkerPositions(schematic);
        } catch (Exception e) {
            throw new ArenaLoadException(e);
        }
        instances.put(buildId, instance);

        return new BuildPlacement<>(instance, Position.ZERO, markers);
    }

    @Override
    public void freeArena(Arena<Instance, ?> arena) throws ArenaLoadException {}

    public static @NotNull List<Marker> extractMarkerPositions(@NotNull Schematic schematic) {
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
                    new Position(point.x(), point.y(), point.z())));
        }

        return markers;
    }

    private static List<String> extractMarkerLines(CompoundBinaryTag compoundBinaryTag) {
        CompoundBinaryTag frontText = compoundBinaryTag.getCompound("front_text");
        ListBinaryTag messages = frontText.getList("messages");
        List<String> result = new ArrayList<>(messages.size());
        for (BinaryTag tag : messages) {
            if (tag instanceof StringBinaryTag line) {
                String value = line.value();
                if(value.length() < 2) continue;
                logger.debug("Extracted marker line: {}", value);
                result.add(value);
            }
        }
        return result;
    }

    public static MinestomArenaPlatform getInstance() {
        return instance;
    }
}