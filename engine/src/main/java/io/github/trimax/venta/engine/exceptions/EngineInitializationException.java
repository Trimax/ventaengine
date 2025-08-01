package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class EngineInitializationException extends AbstractVentaException {
    public EngineInitializationException(final String message) {
        super(String.format("Can't initialize engine: %s", message));
    }
}
