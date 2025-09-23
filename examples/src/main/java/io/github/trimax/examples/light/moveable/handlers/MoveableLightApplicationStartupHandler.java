package io.github.trimax.examples.light.moveable.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.light.moveable.state.MoveableLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.effects.Attenuation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class MoveableLightApplicationStartupHandler implements VentaEngineStartupHandler {
    private final MoveableLightApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        final var plane = context.getObjectManager().create("XZ plane", context.getObjectRepository().get("plane.json"));
        plane.setMaterial(context.getMaterialRegistry().get("rust.json"));
        plane.setScale(new Vector3f(10f, 1f, 10f));
        scene.add(plane);

        final var light = context.getLightManager().create("XZ light", context.getLightRepository().get("point.json"));
        light.setAttenuation(new Attenuation(1, 0.5f, 0));
        light.setPosition(new Vector3f(0.f, 1.f, 0.f));
        light.setColor(new Vector3f(0.5f, 0.5f, 0.5f));
        light.setIntensity(8.f);
        state.setLight(light);
        scene.add(light);
    }
}
