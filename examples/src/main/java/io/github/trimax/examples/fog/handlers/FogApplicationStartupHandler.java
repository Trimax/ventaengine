package io.github.trimax.examples.fog.handlers;

import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.scene.Fog;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class FogApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int CUBE_COUNT = 10;
    private static final float CIRCLE_RADIUS = 10.0f;
    private final FogApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));
        scene.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));

        scene.setFog(new Fog(10.f, 25.f, new Vector3f(0.6f, 0.6f, 0.6f)));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(-17.0f, 4.0f, 0.0f));
        camera.lookAt(new Vector3f(0.f, 4.f, 0.f));
        state.setCamera(camera);
        state.setFog(scene.getFog());

        for (int cubeID = 0; cubeID < CUBE_COUNT; cubeID++) {
            final var cube = context.getObjectManager().create("cube" + cubeID,
                    context.getObjectRepository().get("cube.json"));

            final float angle = cubeID * (360.0f / CUBE_COUNT);
            final float radians = (float) Math.toRadians(angle);

            cube.setPosition(new Vector3f(
                    CIRCLE_RADIUS * (float) Math.cos(radians),
                    0.0f,
                    CIRCLE_RADIUS * (float) Math.sin(radians)
            ));
            scene.add(cube);
            state.getCubes().add(cube);
        }
    }
}
