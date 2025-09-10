package io.github.trimax.examples.gridmesh.water.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class WaterApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Water demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.6f, 0.6f, 0.6f));
        scene.add(context.getGridMeshManager().create("Water", context.getGridMeshRepository().get("default.json")));
    }
}
