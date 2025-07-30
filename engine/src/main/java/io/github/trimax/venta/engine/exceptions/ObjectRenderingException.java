package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class ObjectRenderingException extends AbstractVentaException {
    public ObjectRenderingException(final String message) {
        super(String.format("Can't render an object: %s", message));
    }
}
