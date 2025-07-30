package io.github.trimax.venta.engine.exceptions;

public final class WindowCreationException extends AbstractVentaException {
    public WindowCreationException(final String message) {
        super(String.format("Can't create window: %s", message));
    }
}
