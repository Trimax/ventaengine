package io.github.trimax.examples.debug.handlers;

import io.github.trimax.examples.debug.state.DebugApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

@AllArgsConstructor
public final class DebugApplicationStartupHandler implements VentaEngineStartupHandler {
    private final DebugApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.6f, 0.6f, 0.6f, 1.f));

        state.setTetrahedron(context.getObjectManager().create("tetrahedron", context.getObjectRepository().get("tetrahedron.json")));
        scene.add(state.getTetrahedron());

        context.getCameraManager().getCurrent().setPosition(new Vector3f(3.f, 3.f, 3.f));
        context.getCameraManager().getCurrent().lookAt(new Vector3f(0.f));

        final var light = context.getLightManager().create("Default light", context.getLightRepository().get("point.json"));
        light.setPosition(new Vector3f(0.f, 2.f, 0.f));
        light.setColor(new Vector3f(1.f, 1.f, 1.f));
        light.setIntensity(2.f);
        scene.add(light);

        final var xCamera = context.getCameraManager().create("X Camera");
        xCamera.setPosition(new Vector3f(2.f, 2.f, 0.f));
        xCamera.lookAt(new Vector3f(0.f));

        final var zCamera = context.getCameraManager().create("Z Camera");
        zCamera.setPosition(new Vector3f(0.f, 2.f, 2.f));
        zCamera.lookAt(new Vector3f(0.f));
    }
}
