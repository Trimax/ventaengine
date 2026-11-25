package io.github.trimax.examples.pointshadow.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.pointshadow.state.PointShadowApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;

/**
 * The lights that fly go around the middle of the ring at different speeds and heights, so the shadows of the pillars turn.
 */
@RequiredArgsConstructor
public final class PointShadowApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private static final float RADIUS = 7.f;
    private static final float SPEED = 0.6f;

    private final Vector3f position = new Vector3f();
    private final PointShadowApplicationState state;

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final var elapsed = (float) time.getTimeElapsed();

        for (int i = 0; i < state.getOrbiting().size(); i++) {
            final var angle = elapsed * SPEED * (i % 2 == 0 ? 1.f : -0.7f) + i * 2.4f;
            final var height = 3.5f + 1.5f * (float) Math.sin(elapsed * 0.9f + i);

            state.getOrbiting().get(i).setPosition(position.set(RADIUS * (float) Math.cos(angle), height, RADIUS * (float) Math.sin(angle)));
        }
    }
}
