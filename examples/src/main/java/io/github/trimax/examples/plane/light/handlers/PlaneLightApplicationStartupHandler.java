package io.github.trimax.examples.plane.light.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class PlaneLightApplicationStartupHandler implements VentaEngineStartupHandler {

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        final var plane = context.getObjectManager().load("plane");

        plane.getMesh().setMaterial(context.getMaterialManager().load("stone"));;
        plane.setScale(new Vector3f(50f));
        scene.add(plane);

        final var light = context.getLightManager().load("basic");
        light.setPosition(new Vector3f(0.f, 1.f, 0.f));
        light.setColor(new Vector3f(1f,0f,0f));
        scene.add(light);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(15.f, 15.f, 15.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
