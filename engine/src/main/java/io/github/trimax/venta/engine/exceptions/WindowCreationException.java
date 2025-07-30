package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class WindowCreationException extends AbstractVentaException {
    public WindowCreationException(final String message) {
        super(String.format("Can't create window: %s", message));
    }
}
