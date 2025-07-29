package com.venta.examples.cube.handlers;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Random;

import org.joml.Vector3f;

import com.venta.engine.interfaces.VentaEngineInputHandler;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class CubeApplicationInputHandler implements VentaEngineInputHandler {
    private final CubeApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        if (action == GLFW_PRESS)
            state.getPushedButtons().add(key);

        if (action == GLFW_RELEASE)
            state.getPushedButtons().remove(key);

        if (action == GLFW_PRESS && key == GLFW_KEY_SPACE) {
            state.getLightXZ().setColor(createRandomVector3());
            state.getLightXY().setColor(createRandomVector3());
            state.getLightYZ().setColor(createRandomVector3());
        }
    }

    @Override
    public void onMouseButton(final int button, final int action, final int mods) {
        log.info("Mouse button: {} action {} mods {}", button, action, mods);
    }

    public Vector3f createRandomVector3() {
        final Random random = new Random();
        random.setSeed(System.nanoTime());
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
