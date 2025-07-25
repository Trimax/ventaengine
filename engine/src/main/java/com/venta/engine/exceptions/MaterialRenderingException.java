package com.venta.engine.exceptions;

public final class MaterialRenderingException extends AbstractVentaException {
    public MaterialRenderingException(final String message) {
        super(String.format("Can't render a material: %s", message));
    }
}
