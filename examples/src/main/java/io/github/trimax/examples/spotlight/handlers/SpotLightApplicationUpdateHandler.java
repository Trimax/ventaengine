package io.github.trimax.examples.spotlight.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.spotlight.state.SpotLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;

/**
 * The cones that sweep turn to a point that goes around the middle of the plane, so the shadows of the pillars turn too.
 */
@RequiredArgsConstructor
public final class SpotLightApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private static final float RADIUS = 9.f;
    private static final float SPEED = 0.5f;

    private final Vector3f target = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final SpotLightApplicationState state;

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final var elapsed = (float) time.getTimeElapsed();

        for (int i = 0; i < state.getSweeping().size(); i++) {
            final var angle = elapsed * SPEED * (i % 2 == 0 ? 1.f : -0.8f) + i * 3.f;
            target.set(RADIUS * (float) Math.cos(angle), 0.f, RADIUS * (float) Math.sin(angle));

            final var light = state.getSweeping().get(i);
            light.setDirection(direction.set(target).sub(light.getPosition()));
        }
    }
}
