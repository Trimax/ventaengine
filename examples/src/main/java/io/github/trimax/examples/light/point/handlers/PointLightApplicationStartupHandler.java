package io.github.trimax.examples.light.point.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class PointLightApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane"));
        plane.setScale(new Vector3f(50f));
        scene.add(plane);

        final var redLight = context.getLightManager().load("basic");
        redLight.setPosition(new Vector3f(3.f, 1.f, 0.f));
        redLight.setColor(new Vector3f(1f, 0f, 0f));
        scene.add(redLight);

        final var blueLight = context.getLightManager().load("basic");
        blueLight.setPosition(new Vector3f(0.f, 1.f, 3.f));
        blueLight.setColor(new Vector3f(0f, 0f, 1f));
        scene.add(blueLight);

        final var greenLight = context.getLightManager().load("basic");
        greenLight.setPosition(new Vector3f(0.f, 1.f, 0.f));
        greenLight.setColor(new Vector3f(0f, 1f, 0f));
        scene.add(greenLight);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(15.f, 15.f, 15.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
