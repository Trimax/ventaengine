package io.github.trimax.examples.gridmesh.terrain.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class TerrainApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Terrain demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.25f));
        scene.setDirectionalLight(new DirectionalLight(new Vector3f(1.f, 0.95f, 0.85f), new Vector3f(-1.f, -0.6f, -0.4f), 1.2f));
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));

        final var terrain = context.getTerrainSurfaceManager().create("Terrain", context.getTerrainSurfaceRepository().get("terrain.json"));
        terrain.setScale(new Vector3f(1.f, 0.5f, 1.f));
        scene.add(terrain);

        // Water in the low places: its depth, the bottom seen through it and the foam of the shore
        final var lake = context.getWaterSurfaceManager().create("Lake", context.getWaterSurfaceRepository().get("lake.json"));
        lake.setPosition(new Vector3f(0.f, -1.f, 0.f));
        lake.setScale(new Vector3f(2.f, 1.f, 2.f));
        scene.add(lake);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0f, 70f, 260f));
        camera.lookAt(new Vector3f(0f));
    }
}
