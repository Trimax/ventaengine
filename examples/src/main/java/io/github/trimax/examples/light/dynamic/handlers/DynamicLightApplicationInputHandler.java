package io.github.trimax.examples.light.dynamic.handlers;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

import java.util.Random;

import org.joml.Vector3f;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class DynamicLightApplicationInputHandler implements VentaEngineInputHandler {
    private final Random random = new Random(System.currentTimeMillis());
    private final DynamicLightApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        random.setSeed(System.nanoTime());

        if (action == GLFW_PRESS && key == GLFW_KEY_SPACE) {
            state.getLightXZ().setColor(createRandomVector3());
            state.getLightXY().setColor(createRandomVector3());
            state.getLightYZ().setColor(createRandomVector3());
        }
    }

    private Vector3f createRandomVector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
