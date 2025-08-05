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
public final class SceneSelectExecutor extends AbstractSceneExecutor {
    private SceneSelectExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "select", "selects existing scene as current");
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
            getConsole().error("Scene <%s> can't be selected because it does not exist", command.asArgument().value());
            return;
        }

        sceneManager.setCurrent(scene);
        getConsole().info("Scene <%s> selected", scene.getID());
    }
}
