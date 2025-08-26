package io.github.trimax.examples.skybox.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import org.joml.Vector4f;

@AllArgsConstructor
public final class SkyboxApplicationStartupHandler implements VentaEngineStartupHandler {
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setCubemap(context.getCubemapRegistry().get("stars.json"));
        scene.setAmbientLight(new Vector4f(1.f));
    }
}
