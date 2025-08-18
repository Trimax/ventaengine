package io.github.trimax.examples.light.point.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.parameters.LightParameters;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@AllArgsConstructor
public final class PointLightApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane.json"));
        plane.setScale(new Vector3f(50f));
        scene.add(plane);

        final var redLight = context.getLightManager().create("Red light", LightParameters.builder()
                .type(LightType.Point)
                .color(new Vector3f(1f, 0f, 0f))
                .build());
        redLight.setPosition(new Vector3f(3.f, 1.f, 0.f));
        scene.add(redLight);

        final var blueLight = context.getLightManager().create("Blue light", LightParameters.builder()
                .type(LightType.Point)
                .color(new Vector3f(0f, 0f, 1f))
                .build());
        blueLight.setPosition(new Vector3f(0.f, 1.f, 3.f));
        scene.add(blueLight);

        final var greenLight = context.getLightManager().create("Green light", LightParameters.builder()
                .type(LightType.Point)
                .color(new Vector3f(0f, 1f, 0f))
                .build());
        greenLight.setPosition(new Vector3f(0.f, 1.f, 0.f));
        scene.add(greenLight);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(15.f, 15.f, 15.f));
        camera.lookAt(new Vector3f(0.f));
    }
}
