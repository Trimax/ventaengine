package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.engine.model.states.AbstractState;
import lombok.NonNull;

public abstract class AbstractController<S extends AbstractState, A> {
    private S state;

    public final void initialize() {
        initialize(null);
    }

    public final void initialize(final A argument) {
        if (state != null)
            return;

        state = create(argument);
    }

    public final S get() {
        return state;
    }

    public final void deinitialize() {
        if (state != null)
            destroy(state);

        state = null;
    }

    protected abstract S create(final A argument);

    protected abstract void destroy(@NonNull final S object);
}
