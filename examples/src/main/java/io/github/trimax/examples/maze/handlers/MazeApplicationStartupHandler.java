package io.github.trimax.examples.maze.handlers;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.effects.Attenuation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class MazeApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int CUBE_SIZE = 20;
    private final MazeApplicationState state;

    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.2f, 0.2f, 0.2f));
        scene.setSkybox(context.getCubemapRegistry().get("stars.json"));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(-1.f, 0.5f, 10.f));
        camera.lookAt(new Vector3f(0.f, 0.5f, 10.f));
        state.setCamera(camera);

        final var light = context.getLightManager().create("Flashlight", context.getLightRepository().get("point.json"));
        light.setIntensity(2.0f);
        light.setAttenuation(new Attenuation(1.0f, 1.5f, 2.0f));
        state.setLight(light);
        scene.add(light);

        final var maze = new Maze(ResourceUtil.loadAsString("/maze/maze.txt"), CUBE_SIZE, CUBE_SIZE);
        for (int x = 0; x < CUBE_SIZE; x++)
            for (int z = 0; z < CUBE_SIZE; z++) {
                if (maze.hasCube(x, z))
                    placeCube(x, z, context, scene);

                if (maze.hasChest(x, z))
                    placeChest(x, z, context, scene);
            }

        final var floor = context.getObjectManager().create("Floor", context.getObjectRepository().get("plane.json"));
        floor.setPosition(new Vector3f(10f, 0f, 10f));
        floor.setScale(new Vector3f(20f, 1f, 20f));
        scene.add(floor);

        context.getMaterialRegistry().get("concrete.json").setTiling(new Vector2f(15f));
    }

    private void placeCube(final int x, final int z, final VentaContext context, final SceneInstance scene) {
        final var cube = context.getObjectManager().create("cube" + (x * CUBE_SIZE + z),
                context.getObjectRepository().get("cube.json"));
        cube.setPosition(new Vector3f(x + 0.5f, 0.5f, z + 0.5f));
        cube.setMaterial(context.getMaterialRegistry().get("stone.json"));
        scene.add(cube);
    }

    private void placeChest(final int x, final int z, final VentaContext context, final SceneInstance scene) {
        final var chest = context.getObjectManager().create("chest" + (x * CUBE_SIZE + z),
                context.getObjectRepository().get("chest.json"));
        chest.setRotation(new Vector3f(0f, (float) Math.PI / 4, 0f));
        chest.setPosition(new Vector3f(x + 0.5f, 0.15f, z + 0.5f));
        chest.scale(0.3f);
        scene.add(chest);
    }

    private record Maze(String value, int width, int length) {
        private Maze(final String value, final int width, final int length) {
            this.value = value.replaceAll("\\R", "");
            this.width = width;
            this.length = length;

            if (this.value.length() != width * length)
                throw new IllegalArgumentException("The length of the value must equal to " + width * length);
        }

        public boolean hasCube(final int x, final int z) {
            return withinBounds(x, width) && withinBounds(z, length) && value.charAt(x * length + z) == '1';
        }

        public boolean hasChest(final int x, final int z) {
            return withinBounds(x, width) && withinBounds(z, length) && value.charAt(x * length + z) == 'X';
        }

        private boolean withinBounds(final int value, final int max) {
            return value >= 0 && value < max;
        }
    }
}
