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
public final class SceneDeleteExecutor extends AbstractSceneExecutor {
    private SceneDeleteExecutor(@NonNull final InternalVentaContext context) {
        super(context, "delete", "deletes existing scene");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        final var sceneManager = getManagers().get(SceneManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var scene = sceneManager.get(command.asArgument().value());
        if (scene == null) {
            getConsole().error("Scene %s can't be deleted because it does not exist", command.asArgument().value());
            return;
        }

        if (scene == sceneManager.getCurrent()) {
            getConsole().error("Scene %s can't be deleted because it is currently selected", command.asArgument().value());
            return;
        }

        sceneManager.delete(scene);
        getConsole().info("Scene '%s' deleted", scene.getID());
    }
}
