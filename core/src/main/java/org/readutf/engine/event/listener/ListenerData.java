package org.readutf.engine.event.listener;



public class ListenerData {

    private final Class<?> type;
    private final GameListener gameListener;

    private ListenerData(Class<?> type, GameListener gameListener) {
        this.type = type;
        this.gameListener = gameListener;
    }

    public static ListenerData of(Class<?> type, GameListener gameListener) {
        return new ListenerData(type, gameListener);
    }

    public static <T> ListenerData typed(Class<T> type, TypedGameListener<T> gameListener) {
        return of(type, gameListener);
    }

    public Class<?> getType() {
        return type;
    }

    public GameListener getGameListener() {
        return gameListener;
    }
}