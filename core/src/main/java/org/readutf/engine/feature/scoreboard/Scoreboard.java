package org.readutf.engine.feature.scoreboard;

import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public interface Scoreboard {
    Component getTitle(UUID playerId);

    List<Component> getLines(UUID playerId);
}