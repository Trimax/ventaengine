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
public final class SceneInfoExecutor extends AbstractSceneExecutor {
    private SceneInfoExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "info", "prints information about selected scene");
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
    }
}
