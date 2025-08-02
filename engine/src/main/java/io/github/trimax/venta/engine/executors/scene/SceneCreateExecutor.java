package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneCreateExecutor extends AbstractSceneExecutor {
    private SceneCreateExecutor(@NonNull final InternalVentaContext context) {
        super(context, "create", "creates a new scene");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: scene create <name>");
            return;
        }

        final var scene = getContext().getSceneManager().create(command.value());
        getConsole().info("Scene '%s' created: %s", command.value(), scene.getID());
    }
}
