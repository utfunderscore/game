package org.readutf.engine.event.adapter.impl;

import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.event.adapter.EventGameAdapter;

public class GameEventAdapter implements EventGameAdapter {

    @Override
    public @Nullable Game convert(Object event) {
        if (event instanceof GameEvent gameEvent) {
            return gameEvent.getGame();
        }
        return null;
    }
}
