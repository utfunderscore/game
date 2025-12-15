package org.readutf.engine.minestom;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.types.Position;

public class PlatformUtils {

    @Contract("_ -> new")
    public static @NotNull Position fromPos(@NotNull Pos pos) {
        return new Position(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
    }

    @Contract("_ -> new")
    public static @NotNull Pos toPos(@NotNull Position pos) {
        return new Pos(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
    }

}
