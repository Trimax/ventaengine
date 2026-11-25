package io.github.trimax.examples.beach.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.beach.state.BeachApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;

/**
 * The camera stands where it stands and looks at the sea, and sways a little: a few slow waves of different periods add up, so
 * the sway doesn't repeat and doesn't look like a machine (the head turns by a fraction of a degree, the eyes go up and down by
 * a couple of centimeters, like a breath).
 */
@RequiredArgsConstructor
public final class BeachApplicationUpdateHandler implements VentaEngineUpdateHandler {
    /** Where the camera is: on the sand, 2.75 meters over the ground (a person on a low dune), and where it looks (the angles are from -z, to +x, in radians) */
    private static final float POSITION_X = 0.f;
    private static final float POSITION_Y = 3.4f;
    private static final float POSITION_Z = 30.f;
    private static final float YAW = (float) Math.toRadians(14.0);
    private static final float PITCH = (float) Math.toRadians(-8.5);

    /** How much the camera sways: the head, in radians, and the eyes, in meters */
    private static final float SWAY_YAW = 0.016f;
    private static final float SWAY_PITCH = 0.009f;
    private static final float SWAY_HEIGHT = 0.05f;
    private static final float SWAY_SIDE = 0.04f;

    private final BeachApplicationState state;

    private final Vector3f position = new Vector3f();
    private final Vector3f target = new Vector3f();

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final var t = (float) time.getTimeElapsed();

        final var yaw = YAW + SWAY_YAW * (float) (Math.sin(t * 0.52) * 0.7 + Math.sin(t * 1.21 + 1.3) * 0.3);
        final var pitch = PITCH + SWAY_PITCH * (float) (Math.sin(t * 0.67 + 1.0) * 0.65 + Math.sin(t * 1.5) * 0.35);

        position.set(POSITION_X + SWAY_SIDE * (float) Math.sin(t * 0.43),
                POSITION_Y + SWAY_HEIGHT * (float) (Math.sin(t * 1.1) * 0.6 + Math.sin(t * 2.6 + 0.5) * 0.4),
                POSITION_Z);

        target.set(position).add(
                (float) (Math.sin(yaw) * Math.cos(pitch)),
                (float) Math.sin(pitch),
                (float) (-Math.cos(yaw) * Math.cos(pitch)));

        state.getCamera().setPosition(position);
        state.getCamera().lookAt(target);
    }
}
