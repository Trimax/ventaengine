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
public final class SceneDeleteExecutor extends AbstractSceneExecutor {
    private SceneDeleteExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "delete", "deletes existing scene");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var sceneManager = getManager(SceneManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var scene = sceneManager.get(command.asArgument().value());
        if (scene == null) {
            getConsole().error("Scene <%s> can't be deleted because it does not exist", command.asArgument().value());
            return;
        }

        if (scene == sceneManager.getCurrent()) {
            getConsole().error("Scene <%s> can't be deleted because it is currently selected", command.asArgument().value());
            return;
        }

        sceneManager.delete(scene);
        getConsole().info("Scene <%s> deleted", scene.getID());
    }
}
