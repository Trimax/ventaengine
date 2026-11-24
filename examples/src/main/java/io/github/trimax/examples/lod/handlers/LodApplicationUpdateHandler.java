package io.github.trimax.examples.lod.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.lod.state.LodApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The camera flies along the rows and back, low over the ground, and every two seconds the log says how many spheres are
 * drawn with each level.
 */
@Slf4j
@RequiredArgsConstructor
public final class LodApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private static final float NEAREST = 12.f;
    private static final float FARTHEST = -120.f;
    private static final float PERIOD = 40.f;
    private static final float HEIGHT = 3.f;
    private static final float REPORT_INTERVAL = 2.f;

    private final Vector3f position = new Vector3f();
    private final LodApplicationState state;

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final var phase = 0.5f * (1.f - (float) Math.cos(2.0 * Math.PI * time.getTimeElapsed() / PERIOD));
        state.getCamera().setPosition(position.set(0.f, HEIGHT, NEAREST + (FARTHEST - NEAREST) * phase));
        state.getCamera().lookAt(new Vector3f(0.f, HEIGHT - 1.f, position.z - 40.f));

        state.setSinceReport(state.getSinceReport() + (float) time.getDelta());
        if (state.getSinceReport() < REPORT_INTERVAL)
            return;

        state.setSinceReport(0.f);
        final var levels = new int[4];
        for (final var sphere : state.getSpheres())
            levels[sphere.getLod()]++;

        log.info("Spheres by level: {} main, {} simpler, {} the simplest, {} cards", levels[0], levels[1], levels[2], levels[3]);
    }
}
