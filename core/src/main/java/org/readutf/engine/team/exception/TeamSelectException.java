package org.readutf.engine.team.exception;

public class TeamSelectException extends Exception{

    public TeamSelectException() {
    }

    public TeamSelectException(String message) {
        super(message);
    }

    public TeamSelectException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamSelectException(Throwable cause) {
        super(cause);
    }
}
