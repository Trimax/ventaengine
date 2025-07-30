package io.github.trimax.venta.engine.exceptions;

public final class ObjectRenderingException extends AbstractVentaException {
    public ObjectRenderingException(final String message) {
        super(String.format("Can't render an object: %s", message));
    }
}
