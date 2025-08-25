package io.github.trimax.examples.fog.handlers;

import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

@AllArgsConstructor
public final class FogApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int CUBE_SIZE = 20;
    private static final float CUBE_SPACING = 2.0f;
    private final FogApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.5f, 0.5f, 0.5f, 1.f));

        scene.setFog(new Fog(new Vector3f(0.8f, 0f, 0f), 0.1f));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 2.5f, 2.f));
        camera.lookAt(new Vector3f(10.f, 0.5f, 0.f));
        state.setCamera(camera);

        final var light = context.getLightManager().create("Flashlight", context.getLightRepository().get("point.json"));
        light.setIntensity(3.0f);
        light.setAttenuation(new Attenuation(1.0f, 1.5f, 2.0f));
        state.setLight(light);
        scene.add(light);

        for (int x = 0; x < CUBE_SIZE; x++)
            placeCube(x, context, scene);
    }

    public void placeCube(final int x, final VentaContext context, final SceneInstance scene) {
        final var cube = context.getObjectManager().create("cube" + (x * CUBE_SIZE),
                context.getObjectRepository().get("cube.json"));
        cube.setPosition(new Vector3f(x * CUBE_SPACING + 0.5f, 0.5f, 0.f));
        scene.add(cube);
    }
}
