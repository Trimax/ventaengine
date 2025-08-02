package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneCreateExecutor extends AbstractSceneExecutor {
    private SceneCreateExecutor(@NonNull final InternalVentaContext context) {
        super(context, "create", "creates a new scene", "name");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        if (StringUtils.isBlank(command.value())) {
            getConsole().warning("Usage: %s", getUsage());
            return;
        }

        final var scene = getContext().getSceneManager().create(command.value());
        getConsole().info("Scene created: %s", scene.getID());
    }
}
