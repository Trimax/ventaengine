package io.github.trimax.examples.terrainlod.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.terrainlod.state.TerrainLodApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class TerrainLodApplicationStartupHandler implements VentaEngineStartupHandler {
    private final TerrainLodApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Terrain levels of detail demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.3f));
        scene.setDirectionalLight(new DirectionalLight(new Vector3f(1.f, 0.95f, 0.85f), new Vector3f(-1.f, -0.6f, -0.4f), 1.4f));
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));

        // The camera sees 1000 units, so the fog starts before the end of the view and the edge of it is not seen
        scene.setFog(new Fog(600.f, 980.f, new Vector3f(0.55f, 0.6f, 0.68f)));

        final var terrain = context.getTerrainSurfaceManager().create("Huge terrain", context.getTerrainSurfaceRepository().get("huge.json"));
        terrain.setDrawMode(state.isWireframe() ? DrawMode.Edge : DrawMode.Polygon);
        scene.add(terrain);
        state.setTerrain(terrain);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 400.f, 0.f));
        state.setCamera(camera);
    }
}
