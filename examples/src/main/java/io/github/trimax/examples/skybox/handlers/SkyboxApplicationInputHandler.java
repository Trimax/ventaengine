package io.github.trimax.examples.skybox.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.skybox.state.SkyboxApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SkyboxApplicationInputHandler implements VentaEngineInputHandler {
    private final SkyboxApplicationState state;

    private static final float sensitivity = 0.1f;

    private float yaw = -90f;
    private float pitch = 0f;

    private float lastX = -1f;
    private float lastY = -1f;
    private boolean firstMouse = true;

    @Override
    public void onMouseMove(final double x, final double y) {
        final var mouseX = (float) x;
        final var mouseY = (float) y;

        if (firstMouse) {
            lastX = mouseX;
            lastY = mouseY;
            firstMouse = false;
            return;
        }

        final var deltaX = mouseX - lastX;
        final var deltaY = mouseY - lastY;

        lastX = mouseX;
        lastY = mouseY;

        yaw += deltaX * sensitivity;
        pitch = Math.clamp(pitch - deltaY * sensitivity, -85, 85);

        final var front = new Vector3f();
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.normalize();

        state.getCamera().lookAt(new Vector3f(state.getCamera().getPosition()).add(front));
    }
}
