package io.github.trimax.examples.cube.coloured.material.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class ColouredCubeApplicationStartupHandler implements VentaEngineStartupHandler {

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Cube drawing started");

        final var scene = context.getSceneManager().getCurrent();

        final var cube = context.getObjectManager().load("colouredcube");
        cube.getMesh().setMaterial(context.getMaterialManager().load("stone"));
        cube.setScale(new Vector3f(5.f));
        scene.add(cube);

        final var lightXZ = context.getLightManager().load("basic");
        lightXZ.setPosition(new Vector3f(3.f, 3.f, 3.f));
        lightXZ.setIntensity(6.0f);
        scene.add(lightXZ);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
