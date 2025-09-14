package io.github.trimax.examples.gridmesh.water.handlers;

import io.github.trimax.examples.gridmesh.water.state.WaterApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
@RequiredArgsConstructor
public final class WaterApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private static final float MINIMAL_CAMERA_DISTANCE = 2.f;
    private final WaterApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        if (context.isButtonPushed(GLFW_KEY_DOWN))
            state.setCameraDistance(state.getCameraDistance() + (float) time.getDelta());

        if (context.isButtonPushed(GLFW_KEY_UP))
            state.setCameraDistance(Math.max(state.getCameraDistance() - (float) time.getDelta(), MINIMAL_CAMERA_DISTANCE));

        if (context.isButtonPushed(GLFW_KEY_LEFT))
            state.setCameraAngle(state.getCameraAngle() + (float) time.getDelta());

        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            state.setCameraAngle(state.getCameraAngle() - (float) time.getDelta());

        state.getCamera().lookAt(new Vector3f((float) Math.sin(state.getCameraAngle()) * state.getCameraDistance(), 5, (float) Math.cos(state.getCameraAngle()) * state.getCameraDistance()));
    }
}
