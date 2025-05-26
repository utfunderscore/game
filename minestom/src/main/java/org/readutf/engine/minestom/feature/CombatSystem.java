package org.readutf.engine.minestom.feature;

import io.github.togar2.pvp.MinestomPvP;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import org.readutf.engine.Game;
import org.readutf.engine.feature.System;
import org.readutf.engine.stage.Stage;

public class CombatSystem implements System {

    static {
        MinestomPvP.init();
    }

    public CombatSystem(Stage<?, ?, ?> stage) {

        EventNode<Event> eventNode = EventNode.value("combat-node", EventFilter.from(Event.class, Event.class, event -> event), event -> {
                    Game<?, ?, ?> eventGame = stage.getGame().getEventManager().getGameFromEvent(event);
                    return eventGame != null && eventGame == stage.getGame() && eventGame.getCurrentStage() == stage;
        });

        CombatFeatureSet featureSet = CombatFeatures.empty()
                .version(CombatVersion.MODERN)
                .add(CombatFeatures.VANILLA_FALL)
                .add(CombatFeatures.VANILLA_PLAYER_STATE)
                .build();

        eventNode.addChild(featureSet.createNode());

    }
}
