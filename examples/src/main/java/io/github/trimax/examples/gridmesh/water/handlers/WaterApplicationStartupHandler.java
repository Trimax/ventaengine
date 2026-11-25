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
        scene.setAmbientLight(new Vector3f(0.5f));

        final var water = context.getWaterSurfaceManager().create("Water", context.getWaterSurfaceRepository().get("water.json"));
        water.setPosition(new Vector3f(0f, 1.5f, 0f));
        scene.add(water);

        // Pillars that stand in the water: they are reflected in it and the water is white with foam around them
        final var cube = context.getObjectRepository().get("cube.json");
        for (int i = 0; i < 6; i++) {
            final var angle = (float) Math.PI / 4.f + (i - 2.5f) * 0.22f;
            final var distance = 16.f + (i % 3) * 7.f;
            final var pillar = context.getObjectManager().create("Pillar " + i, cube);
            pillar.setScale(new Vector3f(2.f, 9.f + i, 2.f));
            pillar.setPosition(new Vector3f((float) Math.sin(angle) * distance, 1.5f + (9.f + i) / 2.f - 2.f, (float) Math.cos(angle) * distance));
            scene.add(pillar);
        }

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0f, 5f, 0f));
        camera.lookAt(new Vector3f(-1f, 5f, -1f));
        state.setCamera(camera);

        final var sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-5f, -6.5f, -3f));
        sun.setColor(new Vector3f(1.0f, 0.6f, 0.4f));
        sun.setIntensity(0.5f);
        scene.setDirectionalLight(sun);
    }
}
