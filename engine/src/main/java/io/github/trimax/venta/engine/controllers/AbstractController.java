package io.github.trimax.venta.engine.controllers;

import lombok.NonNull;

public abstract class AbstractController<O, A> {
    private O object;

    public final void initialize() {
        initialize(null);
    }

    public final void initialize(final A argument) {
        if (object != null)
            return;

        object = create(argument);
    }

    public final O get() {
        return object;
    }

    public final void deinitialize() {
        if (object != null)
            destroy(object);

        object = null;
    }

    protected abstract O create(final A argument);

    protected abstract void destroy(@NonNull final O object);
}
