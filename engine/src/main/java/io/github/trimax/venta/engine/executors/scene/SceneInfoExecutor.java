package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneInfoExecutor extends AbstractSceneExecutor {
    private SceneInfoExecutor(@NonNull final InternalVentaContext context) {
        super(context, "info", "prints information about selected scene");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        final var scene = getManagers().get(SceneManagerImplementation.class).getCurrent();
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
    }
}
