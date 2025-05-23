package org.readutf.engine.arena;

import java.util.Objects;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.markers.Position;

import java.util.List;

@Getter
public final class Arena<WORLD, T extends BuildFormat> {
    @Getter private final int arenaId;
    private @NotNull final Build build;
    private @NotNull final WORLD world;
    private @NotNull final T format;
    private @NotNull final List<Position> positions;

    public Arena(
            int arenaId,
            @NotNull Build build,
            @NotNull WORLD world,
            @NotNull T format,
            @NotNull List<Position> positions
    ) {
        this.arenaId = arenaId;
        this.build = build;
        this.world = world;
        this.format = format;
        this.positions = positions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Arena) obj;
        return this.arenaId == that.arenaId &&
                Objects.equals(this.build, that.build) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.format, that.format) &&
                Objects.equals(this.positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arenaId, build, world, format, positions);
    }

    @Override
    public String toString() {
        return "Arena[" +
                "arenaId=" + arenaId + ", " +
                "build=" + build + ", " +
                "world=" + world + ", " +
                "format=" + format + ", " +
                "positions=" + positions + ']';
    }

}
