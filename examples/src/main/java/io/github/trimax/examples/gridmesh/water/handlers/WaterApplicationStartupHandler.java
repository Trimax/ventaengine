package io.github.trimax.examples.gridmesh.water.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.gridmesh.water.state.WaterApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class WaterApplicationStartupHandler implements VentaEngineStartupHandler {
    private final WaterApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Water demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("sunset.json"));
        scene.setAmbientLight(new Vector3f(1.f));

        final var water = context.getWaterSurfaceManager().create("Water", context.getWaterSurfaceRepository().get("water.json"));
        water.setPosition(new Vector3f(0f, 1.5f, 0f));
        scene.add(water);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0f, 5f, 0f));
        camera.lookAt(new Vector3f(-1f, 5f, -1f));
        state.setCamera(camera);

        final var sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-5f, -6.5f, -3f));
        sun.setColor(new Vector3f(1.0f, 0.6f, 0.4f));
        sun.setIntensity(1.f);
        scene.setDirectionalLight(sun);
    }
}
