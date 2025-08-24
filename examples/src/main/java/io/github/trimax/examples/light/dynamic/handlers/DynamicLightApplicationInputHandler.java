package io.github.trimax.examples.light.dynamic.handlers;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@Slf4j
@AllArgsConstructor
public final class DynamicLightApplicationInputHandler implements VentaEngineInputHandler {
    private final DynamicLightApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
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
        final Random random = new Random(System.currentTimeMillis());
        random.setSeed(System.nanoTime());
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
