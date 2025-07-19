package com.venta.engine.exceptions;

public final class ResourceNotFoundException extends VentaException {
    public ResourceNotFoundException(final String resourceName) {
        super(String.format("Can't load resource %s", resourceName));
    }
}
