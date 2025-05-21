package org.readutf.engine.event.exceptions;

public class EventDispatchException extends Exception {
    public EventDispatchException(String message) {
        super(message);
    }

    public EventDispatchException() {}

    public EventDispatchException(Throwable cause) {
        super(cause);
    }

    public EventDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
