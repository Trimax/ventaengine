package io.github.trimax.venta.engine.exceptions;

public final class ResourceNotFoundException extends AbstractVentaException {
    public ResourceNotFoundException(final String resourceName) {
        super(String.format("Can't load resource %s", resourceName));
    }
}
