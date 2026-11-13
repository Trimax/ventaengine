package io.github.trimax.examples.scene.handlers;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;

import io.github.trimax.examples.scene.state.SceneApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;

/**
 * The camera flies slowly around the castle (the arrows turn it and change the distance), the campfire light flickers.
 */
@RequiredArgsConstructor
public final class SceneApplicationUpdateHandler implements VentaEngineUpdateHandler {
    /* A full circle takes about 90 seconds */
    private static final float ORBIT_SPEED = (float) (2.0 * Math.PI / 90.0);
    private static final float MANUAL_SPEED = 0.5f;
    private static final float ZOOM_SPEED = 40.f;

    private static final float MINIMAL_DISTANCE = 40.f;
    private static final float MAXIMAL_DISTANCE = 220.f;

    /*
     * The camera floats up and down around this height above the target and never goes below the ground clearance.
     * It stays 11-17 units below the top of the fog layer (see scenes/demo.json): the horizon is hazy and the low sun
     * still shines through.
     */
    private static final float CAMERA_HEIGHT = 30.f;
    private static final float CAMERA_HEIGHT_SWING = 3.f;
    private static final float GROUND_CLEARANCE = 8.f;

    private static final float CAMPFIRE_INTENSITY = 6.f;

    private final Vector3f cameraPosition = new Vector3f();
    private final SceneApplicationState state;

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final var delta = (float) time.getDelta();
        final var elapsed = (float) time.getTimeElapsed();

        updateControls(context, delta);
        updateCamera(elapsed);
        updateCampfire(elapsed);
    }

    private void updateControls(final VentaContext context, final float delta) {
        var angle = state.getCameraAngle() + ORBIT_SPEED * delta;
        if (context.isButtonPushed(GLFW_KEY_LEFT))
            angle -= MANUAL_SPEED * delta;

        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            angle += MANUAL_SPEED * delta;

        var distance = state.getCameraDistance();
        if (context.isButtonPushed(GLFW_KEY_UP))
            distance -= ZOOM_SPEED * delta;

        if (context.isButtonPushed(GLFW_KEY_DOWN))
            distance += ZOOM_SPEED * delta;

        state.setCameraAngle(angle);
        state.setCameraDistance(Math.clamp(distance, MINIMAL_DISTANCE, MAXIMAL_DISTANCE));
    }

    private void updateCamera(final float elapsed) {
        final var target = state.getCameraTarget();
        final var x = target.x + (float) Math.sin(state.getCameraAngle()) * state.getCameraDistance();
        final var z = target.z + (float) Math.cos(state.getCameraAngle()) * state.getCameraDistance();
        final var y = target.y + CAMERA_HEIGHT + CAMERA_HEIGHT_SWING * (float) Math.sin(elapsed * 0.15f);

        state.getCamera().setPosition(cameraPosition.set(x, Math.max(y, state.getTerrain().getHeight(x, z) + GROUND_CLEARANCE), z));
        state.getCamera().lookAt(target);
    }

    private void updateCampfire(final float elapsed) {
        final var flicker = 0.8f + 0.12f * (float) Math.sin(elapsed * 13.f) + 0.08f * (float) Math.sin(elapsed * 29.f + 1.3f);
        state.getCampfireLight().setIntensity(CAMPFIRE_INTENSITY * flicker);
    }
}
