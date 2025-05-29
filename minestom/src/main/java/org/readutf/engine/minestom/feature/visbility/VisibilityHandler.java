package org.readutf.engine.minestom.feature.visbility;

import net.minestom.server.entity.Player;

public interface VisibilityHandler {

    boolean isVisibleToPlayer(Player viewer, Player player);

}
