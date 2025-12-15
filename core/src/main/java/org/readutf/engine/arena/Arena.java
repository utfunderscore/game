package org.readutf.engine.arena;

import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.BuildMeta;
import org.readutf.buildformat.types.Position;

import java.util.List;
import java.util.Objects;


public class Arena<WORLD, T> {
    private final int arenaId;
    private @NotNull
    final BuildMeta buildMeta;
    private @NotNull
    final WORLD world;
    private @NotNull
    final T format;

    public Arena(
            int arenaId,
            @NotNull BuildMeta buildMeta,
            @NotNull WORLD world,
            @NotNull T format
    ) {
        this.arenaId = arenaId;
        this.buildMeta = buildMeta;
        this.world = world;
        this.format = format;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Arena) obj;
        return this.arenaId == that.arenaId &&
                Objects.equals(this.buildMeta, that.buildMeta) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arenaId, buildMeta, world, format);
    }

    @Override
    public String toString() {
        return "Arena[" +
                "arenaId=" + arenaId + ", " +
                "build=" + buildMeta + ", " +
                "world=" + world + ", " +
                "format=" + format + ", " + ']';
    }

    public int getArenaId() {
        return arenaId;
    }

    public @NotNull BuildMeta getBuildMeta() {
        return buildMeta;
    }

    public @NotNull WORLD getWorld() {
        return world;
    }

    public @NotNull T getFormat() {
        return format;
    }
}
