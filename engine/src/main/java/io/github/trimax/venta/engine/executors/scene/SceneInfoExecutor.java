package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.helpers.TerrainChunkHelper;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneInfoExecutor extends AbstractSceneExecutor {
    private final GeometryHelper geometryHelper;
    private final TerrainChunkHelper terrainChunkHelper;

    private SceneInfoExecutor(@NonNull final ControllerFactory factory, @NonNull final GeometryHelper geometryHelper,
                              @NonNull final TerrainChunkHelper terrainChunkHelper) {
        super(factory, "info", "prints information about selected scene");

        this.geometryHelper = geometryHelper;
        this.terrainChunkHelper = terrainChunkHelper;
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var scene = getManager(SceneManagerImplementation.class).getCurrent();
        if (scene == null) {
            getConsole().error("The scene is not selected");
            return;
        }

        getConsole().header("Scene <%s>:", scene.getID());
        getConsole().info("           ID: %s", scene.getID());
        getConsole().info("         Name: %s", scene.getName());
        getConsole().info("Objects count: %s", scene.getObjects().size());
        getConsole().info(" Lights count: %s", scene.getLights().size());
        getConsole().info("      Ambient: %s", scene.getAmbientLight());
        getConsole().info("       Skybox: %b", scene.getSkybox() != null);
        getConsole().info("          Fog: %s", scene.getFog());
        getConsole().info(" Antialiasing: %s", describe(scene.getAntialiasing()));
        getConsole().info("Terrain tris: %s (last frame, all the passes)", terrainChunkHelper.getTriangles());
        getConsole().info("   Draw calls: %s (last frame, with shadows)", geometryHelper.getDrawCalls());
    }

    private static String describe(final Antialiasing antialiasing) {
        if (antialiasing == null || !antialiasing.isActive())
            return "none";

        return String.format("%s, %s quality, subpixel %s, edge threshold %s", antialiasing.getType(), antialiasing.getQuality(),
                antialiasing.getSubpixel(), antialiasing.getEdgeThreshold());
    }
}
