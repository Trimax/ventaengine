package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneDescribeExecutor extends AbstractSceneExecutor {
    private SceneDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the scene");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var sceneManager = getManager(SceneManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var scene = sceneManager.getInstance(command.asArgument().value());
        if (scene == null) {
            getConsole().error("Scene <%s> can't be described because it does not exist", command.asArgument().value());
            return;
        }

        getConsole().header("Scene <%s>:", scene.getID());
        getConsole().info("                           ID: %s", scene.getID());
        getConsole().info("                         Name: %s", scene.getName());
        getConsole().info("                Objects count: %s", scene.getObjects().size());
        getConsole().info("                 Lights count: %s", scene.getLights().size());
        getConsole().info("                Ambient light: %s", scene.getAmbientLight());
        getConsole().info("            Directional light: %s", scene.getDirectionalLight() != null);

        final var directionalLight = scene.getDirectionalLight();
        if (directionalLight != null) {
            getConsole().info("  Directional light color: %s", directionalLight.getColor());
            getConsole().info("  Directional light direction: %s", directionalLight.getDirection());
            getConsole().info("  Directional light intensity: %s", directionalLight.getIntensity());
        }
    }
}
