package io.github.trimax.examples.reflections.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.reflections.state.ReflectionsApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        final var cubeRust = context.getObjectManager().create("Cube like metal", context.getObjectRepository().get("cube.json"));
        cubeRust.setPosition(new Vector3f(0.f, -1.f, 0.f));
        cubeRust.setMaterial(context.getMaterialRegistry().get("rust.json"));
        scene.add(cubeRust);

        final var cubeMetal = context.getObjectManager().create("Cube like metal", context.getObjectRepository().get("cube.json"));
        cubeMetal.setPosition(new Vector3f(0.f, -2.f, 0.f));
        cubeMetal.setMaterial(context.getMaterialRegistry().get("metal.json"));
        scene.add(cubeMetal);

        final var cube = context.getObjectManager().create("Cube", context.getObjectRepository().get("cube.json"));
        cube.setPosition(new Vector3f(0.f, -3.f, 0.f));
        scene.add(cube);

        final var camera = context.getCameraManager().getCurrent();
        state.setCamera(camera);
    }
}
