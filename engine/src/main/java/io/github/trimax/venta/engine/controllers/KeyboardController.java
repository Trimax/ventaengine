package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.states.KeyboardState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyboardController extends AbstractController<KeyboardState, Void> {
    @Override
    protected KeyboardState create(final Void argument) {
        log.info("Initializing keyboard");

        return new KeyboardState();
    }

    @Override
    protected void destroy(@NonNull final KeyboardState object) {
        log.info("Deinitializing keyboard");
    }
}
