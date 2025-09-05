package io.github.trimax.examples.reflections.handlers;

import io.github.trimax.examples.reflections.state.ReflectionsApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@AllArgsConstructor
public final class ReflectionsApplicationStartupHandler implements VentaEngineStartupHandler {
    private final ReflectionsApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Reflections demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.6f, 0.6f, 0.6f));
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));

        final var cubeMirror = context.getObjectManager().create("Cube like mirror", context.getObjectRepository().get("cube.json"));
        cubeMirror.setMaterial(context.getMaterialRegistry().get("mirror.json"));
        scene.add(cubeMirror);

        final var cube = context.getObjectManager().create("Cube", context.getObjectRepository().get("cube.json"));
        cube.setPosition(new Vector3f(0.f, -1.f, 0.f));
        scene.add(cube);

        final var camera = context.getCameraManager().getCurrent();
        state.setCamera(camera);
    }
}
