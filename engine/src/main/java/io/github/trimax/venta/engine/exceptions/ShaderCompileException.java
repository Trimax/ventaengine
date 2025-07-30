package io.github.trimax.venta.engine.exceptions;

public final class ShaderCompileException extends AbstractVentaException {
    public ShaderCompileException(final String message) {
        super(String.format("Can't compile shader: %s", message));
    }
}
