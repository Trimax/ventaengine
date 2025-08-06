package io.github.trimax.examples.maze.handlers;

import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

@AllArgsConstructor
public final class MazeApplicationStartupHandler implements VentaEngineStartupHandler {
    private final MazeApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector4f(0.2f, 0.2f, 0.2f, 1.f));

        final String noNewLines = ResourceUtil.loadAsString("/maze/maze.txt").replaceAll("\\R", "");

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(-1.f, 0.5f, 10.f));
        camera.lookAt(new Vector3f(0.f, 0.5f, 10.f));
        state.setCamera(camera);

        final var light = context.getLightManager().load("basic");
        light.setIntensity(3.0f);
        light.setAttenuation(new LightInstance.Attenuation(1.0f, 1.5f, 2.0f));
        state.setLight(light);
        scene.add(light);

        final int cubeCount = 20;
        for (int x = 0; x < cubeCount; x++) {
            for (int z = 0; z < cubeCount; z++) {
                if (noNewLines.charAt(x * cubeCount + z) == '0')
                    continue;
                final var miniCube = context.getObjectManager().create("cube" + (x * cubeCount + z),
                        context.getObjectRepository().get("cube"));
                miniCube.setPosition(new Vector3f(x + 0.5f, 0.5f, z + 0.5f));
                scene.add(miniCube);
            }
        }
    }
}
