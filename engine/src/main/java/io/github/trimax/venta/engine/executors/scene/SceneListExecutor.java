package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneListExecutor extends AbstractSceneExecutor {
    private SceneListExecutor(@NonNull final InternalVentaContext context) {
        super(context, "list", "shows scenes list");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getConsole().info("Scenes:");
        final var sceneManager = getManagers().get(SceneManagerImplementation.class);
        StreamEx.of(sceneManager.iterator())
                .map(AbstractView::getPublicInformation)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
