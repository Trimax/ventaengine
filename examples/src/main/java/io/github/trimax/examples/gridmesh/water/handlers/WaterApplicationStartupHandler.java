package io.github.trimax.examples.gridmesh.water.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class WaterApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Water demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));
        scene.setAmbientLight(new Vector3f(0.6f, 0.6f, 0.6f));

        final var water = context.getGridMeshManager().create("Water", context.getGridMeshRepository().get("water.json"));
        water.setMaterial(context.getMaterialRegistry().get("water.json"));
        water.setPosition(new Vector3f(0f, 1.5f, 0f));
        scene.add(water);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0f, 10f, 250f));
        camera.lookAt(new Vector3f(0f));

        final var light = context.getLightManager().create("Sun", context.getLightRepository().get("point.json"));
        light.setAttenuation(new Attenuation(1, 1, 0));
        light.setPosition(new Vector3f(5f, 5f, -10f));
        light.setColor(new Vector3f(1.f, 0.85f, 0.6f));
        light.setIntensity(5.f);
        scene.add(light);
    }
}
