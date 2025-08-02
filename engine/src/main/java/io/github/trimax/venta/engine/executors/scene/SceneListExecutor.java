package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneListExecutor extends AbstractSceneExecutor {
    private SceneListExecutor(@NonNull final InternalVentaContext context) {
        super(context, "list", "shows scenes list");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        for (int i = 0; i < 10; i++)
            getConsole().info("scene xxx %d", i);
    }
}
