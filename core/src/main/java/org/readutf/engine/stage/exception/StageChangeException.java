package org.readutf.engine.stage.exception;

public class StageChangeException extends RuntimeException {
    public StageChangeException(String message) {
        super(message);
    }

    public StageChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StageChangeException(Throwable cause) {
        super(cause);
    }

    public StageChangeException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StageChangeException() {}
}
