package io.github.trimax.venta.engine.executors.object;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ObjectListExecutor extends AbstractObjectExecutor {
    private ObjectListExecutor(@NonNull final InternalVentaContext context) {
        super(context, "list", "shows objects list");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        getConsole().header("Objects:");

        final var objectManager = getManagers().get(ObjectManagerImplementation.class);
        StreamEx.of(objectManager.iterator())
                .map(AbstractView::getPublicInformation)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
