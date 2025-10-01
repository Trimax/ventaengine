package io.github.trimax.examples.gridmesh.terrain.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class TerrainApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Terrain demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(1.f));

        final var terrain = context.getTerrainSurfaceManager().create("Terrain", context.getTerrainSurfaceRepository().get("terrain.json"));
        terrain.setDrawMode(DrawMode.Edge);
        scene.add(terrain);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5f, 5f, 5f));
        camera.lookAt(new Vector3f(0f));
    }
}
