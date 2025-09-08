package io.github.trimax.examples.light.dynamic.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class DynamicLightApplicationStartupHandler implements VentaEngineStartupHandler {
    private final DynamicLightApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        createCubes(context, scene, context.getObjectRepository().get("cube.json"));
        createLights(context, scene, context.getLightRepository().get("point.json"));
        createPlanes(context, scene, context.getObjectRepository().get("plane.json"));
    }

    private void createPlanes(final VentaContext context, final SceneInstance scene, final ObjectPrefab prefab) {
        final var planeXZ = context.getObjectManager().create("XZ plane", prefab);
        planeXZ.setScale(new Vector3f(10f, 1f, 10f));
        planeXZ.setPosition(new Vector3f(0.f, -3.0f, 0.f));
        scene.add(planeXZ);

        final var planeXY = context.getObjectManager().create("XY plane", prefab);
        planeXY.setRotation(new Vector3f((float) Math.PI / 2, 0f, 0f));
        planeXY.setScale(new Vector3f(10f, 1f, 10f));
        planeXY.setPosition(new Vector3f(0.f, 0.0f, -3.f));
        scene.add(planeXY);

        final var planeYZ = context.getObjectManager().create("YZ plane", prefab);
        planeYZ.setRotation(new Vector3f((float) -Math.PI / 2, 0f, (float) -Math.PI / 2));
        planeYZ.setScale(new Vector3f(10f, 1f, 10f));
        planeYZ.setPosition(new Vector3f(-3.f, 0.0f, 0.f));
        scene.add(planeYZ);
    }

    private void createLights(final VentaContext context, final SceneInstance scene, final LightPrefab prefab) {
        final var lightXZ = context.getLightManager().create("XZ light", prefab);
        lightXZ.setAttenuation(new Attenuation(1, 0.5f, 0));
        lightXZ.setPosition(new Vector3f(2.f, 2.f, 2.f));
        lightXZ.setColor(new Vector3f(0.3f, 0.0f, 0.0f));
        lightXZ.setIntensity(1.f);
        state.setLightXZ(lightXZ);
        scene.add(lightXZ);

        final var lightXY = context.getLightManager().create("XY light", prefab);
        lightXY.setAttenuation(new Attenuation(1, 0.5f, 0));
        lightXY.setPosition(new Vector3f(-2.f, -2.f, 2.f));
        lightXZ.setColor(new Vector3f(0.0f, 0.3f, 0.0f));
        lightXY.setIntensity(1.f);
        state.setLightXY(lightXY);
        scene.add(lightXY);

        final var lightYZ = context.getLightManager().create("YZ light", prefab);
        lightYZ.setAttenuation(new Attenuation(1, 0.5f, 0));
        lightYZ.setPosition(new Vector3f(2.f, -2.f, -2.f));
        lightXZ.setColor(new Vector3f(0.0f, 0.0f, 0.3f));
        lightYZ.setIntensity(1.f);
        state.setLightYZ(lightYZ);
        scene.add(lightYZ);
    }

    private void createCubes(final VentaContext context, final SceneInstance scene, final ObjectPrefab prefab) {
        final var cube = context.getObjectManager().create("Main cube", prefab);
        cube.setScale(new Vector3f(2.f));
        state.setCube(cube);
        scene.add(cube);

        final int cubeCount = 10;
        for (int cubeID = 0; cubeID < cubeCount; cubeID++) {
            final var miniCube = context.getObjectManager().create("cube" + cubeID, prefab);
            miniCube.setMaterial(context.getMaterialRegistry().get("stone.json"));
            miniCube.setPosition(new Vector3f(
                    3.f * (float) Math.sin(cubeID * (2 * Math.PI / cubeCount)),
                    0.f,
                    3.f * (float) Math.cos(cubeID * (2 * Math.PI / cubeCount))));
            scene.add(miniCube);
        }
    }
}
