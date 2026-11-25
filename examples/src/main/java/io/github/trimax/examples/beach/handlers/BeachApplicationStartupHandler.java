package io.github.trimax.examples.beach.handlers;

import io.github.trimax.examples.beach.state.BeachApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The scene is described in scenes/beach.json; the code only takes the camera that the update handler moves.
 */
@Slf4j
@AllArgsConstructor
public final class BeachApplicationStartupHandler implements VentaEngineStartupHandler {
    private final BeachApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Ocean beach demo started");

        final var scene = context.getSceneManager().create("Ocean beach", context.getSceneRepository().get("beach.json"));
        context.getSceneManager().setCurrent(scene);

        state.setCamera(context.getCameraManager().getCurrent());
    }
}
