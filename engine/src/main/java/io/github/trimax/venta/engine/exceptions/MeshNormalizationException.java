package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class MeshNormalizationException extends AbstractVentaException {
    public MeshNormalizationException(final String message) {
        super(String.format("Can't normalize mesh: %s", message));
    }
}
