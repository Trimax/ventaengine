package com.venta.examples.cube.handlers;

import org.joml.Vector3f;

import com.venta.engine.core.VentaContext;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.VentaEngineStartupHandler;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class CubeApplicationStartupHandler implements VentaEngineStartupHandler {
    private final CubeApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Rotating cube started");

        final var scene = context.getSceneManager().getCurrent();

        final var cube = context.getObjectManager().load("cube");
        cube.getMesh().setMaterial(context.getMaterialManager().load("stone"));
        cube.setScale(new Vector3f(2.f));
        state.setCube(cube);
        scene.add(cube);

        final var light = context.getLightManager().load("basic");
        light.setPosition(new Vector3f(2.f, 2.f, 2.f));
        state.setLight(light);
        scene.add(light);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        final var gizmo = context.getObjectManager().load("gizmo");
        gizmo.setDrawMode(DrawMode.Edge);
        state.setGizmo(gizmo);
        scene.add(gizmo);

        final int cubeCount = 10;
        for (int cubeID = 0; cubeID < cubeCount; cubeID++) {
            final var miniCube = context.getObjectManager().create("cube" + cubeID, cube.getMesh(), cube.getProgram());
            miniCube.setPosition(new Vector3f(
                    3.f * (float) Math.sin(cubeID * (2 * Math.PI / cubeCount)),
                    0.f,
                    3.f * (float) Math.cos(cubeID * (2 * Math.PI / cubeCount))));
            scene.add(miniCube);
        }
    }
}
