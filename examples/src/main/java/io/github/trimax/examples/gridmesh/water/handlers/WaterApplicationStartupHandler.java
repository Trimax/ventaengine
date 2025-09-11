package io.github.trimax.examples.gridmesh.water.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class WaterApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Water demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.6f, 0.6f, 0.6f));

        final var water = context.getGridMeshManager().create("Water", context.getGridMeshRepository().get("water.json"));
        water.setDrawMode(DrawMode.Edge);
        scene.add(water);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0f, 10f, 20f));
        camera.lookAt(new Vector3f(0f));

        final var light = context.getLightManager().create("Sun", context.getLightRepository().get("point.json"));
        light.setPosition(new Vector3f(5f, 2f, -10f));
        //light.setIntensity(10f);
        scene.add(light);
    }
}
