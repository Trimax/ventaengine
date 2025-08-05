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
public final class SceneCreateExecutor extends AbstractSceneExecutor {
    private SceneCreateExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "create", "creates a new scene");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <name>", command.getFullPath());
            return;
        }

        final var scene = getManager(SceneManagerImplementation.class).create(command.value());
        getConsole().info("Scene <%s> created", scene.getID());
    }
}
