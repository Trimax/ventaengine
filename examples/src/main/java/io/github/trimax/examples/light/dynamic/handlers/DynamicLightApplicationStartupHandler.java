package io.github.trimax.examples.light.dynamic.handlers;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class DynamicLightApplicationStartupHandler implements VentaEngineStartupHandler {
    private final DynamicLightApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        final var cubePrefab = context.getObjectRepository().get("cube.json");

        final var cube = context.getObjectManager().create("Main cube", cubePrefab);
        cube.setScale(new Vector3f(2.f));
        state.setCube(cube);
        scene.add(cube);

        final var lightPrefab = context.getLightRepository().get("point.json");

        final var lightXZ = context.getLightManager().create("XZ light", lightPrefab);
        lightXZ.setPosition(new Vector3f(2.f, 2.f, 2.f));
        state.setLightXZ(lightXZ);
        scene.add(lightXZ);

        final var lightXY = context.getLightManager().create("XY light", lightPrefab);
        lightXY.setPosition(new Vector3f(-2.f, -2.f, 2.f));
        state.setLightXY(lightXY);
        scene.add(lightXY);

        final var lightYZ = context.getLightManager().create("YZ light", lightPrefab);
        lightXY.setPosition(new Vector3f(2.f, -2.f, -2.f));
        state.setLightYZ(lightYZ);
        scene.add(lightYZ);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        final int cubeCount = 10;
        for (int cubeID = 0; cubeID < cubeCount; cubeID++) {
            final var miniCube = context.getObjectManager().create("cube" + cubeID, cubePrefab);
            miniCube.setMaterial(context.getMaterialRegistry().get("concrete.json"));
            miniCube.setPosition(new Vector3f(
                    3.f * (float) Math.sin(cubeID * (2 * Math.PI / cubeCount)),
                    0.f,
                    3.f * (float) Math.cos(cubeID * (2 * Math.PI / cubeCount))));
            scene.add(miniCube);
        }
    }
}
