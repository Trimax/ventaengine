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
public final class SceneSelectExecutor extends AbstractSceneExecutor {
    private SceneSelectExecutor(@NonNull final InternalVentaContext context) {
        super(context, "select", "selects existing scene as current");
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
            getConsole().error("Scene %s can't be selected because it does not exist", command.asArgument().value());
            return;
        }

        sceneManager.setCurrent(scene);
        getConsole().info("Scene '%s' selected", scene.getID());
    }
}
