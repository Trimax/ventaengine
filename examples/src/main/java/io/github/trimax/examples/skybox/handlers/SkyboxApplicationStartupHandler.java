package io.github.trimax.examples.skybox.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.skybox.state.SkyboxApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class SkyboxApplicationStartupHandler implements VentaEngineStartupHandler {
    private final SkyboxApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));
        scene.setAmbientLight(new Vector3f(1.f));

        state.setCamera(context.getCameraManager().getCurrent());
    }
}
