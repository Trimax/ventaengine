package io.github.trimax.venta.container.exceptions;

public final class CyclicDependencyException extends AbstractVentaException {
    public CyclicDependencyException(final String message) {
        super(String.format("Cyclic dependency found: %s", message));
    }
}
