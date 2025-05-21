package org.readutf.engine.arena.exception;

public class ArenaLoadException extends Exception {

    public ArenaLoadException() {}

    public ArenaLoadException(String message) {
        super(message);
    }

    public ArenaLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArenaLoadException(Throwable cause) {
        super(cause);
    }
}
