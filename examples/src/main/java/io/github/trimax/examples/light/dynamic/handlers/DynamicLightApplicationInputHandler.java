package io.github.trimax.examples.light.dynamic.handlers;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.utils.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class DynamicLightApplicationInputHandler implements VentaEngineInputHandler {
    private final DynamicLightApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        if (action == GLFW_PRESS && key == GLFW_KEY_SPACE) {
            state.getLightXZ().setColor(RandomUtil.vector3());
            state.getLightXY().setColor(RandomUtil.vector3());
            state.getLightYZ().setColor(RandomUtil.vector3());
        }
    }
}
