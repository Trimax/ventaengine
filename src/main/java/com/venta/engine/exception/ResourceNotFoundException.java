package com.venta.engine.exception;

public final class ResourceNotFoundException extends VentaException {
    public ResourceNotFoundException(String resourceName) {
        super(String.format("Can't load resource %s", resourceName));
    }
}
