package org.readutf.engine.arena;

import java.util.Objects;
import org.readutf.buildformat.common.Build;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.markers.Position;

import java.util.List;

public class Arena<WORLD, T extends BuildFormat> {
    private final int arenaId;
    private final Build build;
    private final WORLD world;
    private final BuildFormat format;
    private final List<Position> positions;

    public Arena(
            int arenaId,
            Build build,
            WORLD world,
            BuildFormat format,
            List<Position> positions
    ) {
        this.arenaId = arenaId;
        this.build = build;
        this.world = world;
        this.format = format;
        this.positions = positions;
    }

    public int getArenaId() {
        return arenaId;
    }

    public Build getBuild() {
        return build;
    }

    public WORLD getWorld() {
        return world;
    }

    public BuildFormat getFormat() {
        return format;
    }

    public List<Position> getPositions() {
        return positions;
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
