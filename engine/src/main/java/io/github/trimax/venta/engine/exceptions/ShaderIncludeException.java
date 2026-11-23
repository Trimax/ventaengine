package io.github.trimax.venta.engine.exceptions;

import io.github.trimax.venta.container.exceptions.AbstractVentaException;

public final class ShaderIncludeException extends AbstractVentaException {
    public ShaderIncludeException(final String message) {
        super(String.format("Can't process shader includes: %s", message));
    }

    public ShaderIncludeException(final String message, final Throwable cause) {
        super(String.format("Can't process shader includes: %s", message), cause);
    }
}
