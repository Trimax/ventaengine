package io.github.trimax.venta.engine.executors;

import java.util.List;
import java.util.Map;

import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import io.github.trimax.venta.engine.core.VentaContext;
import io.github.trimax.venta.engine.core.VentaState;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExecutor {
    private final Map<String, ? extends AbstractExecutor> executors;
    private final InternalVentaContext internalContext;

    @Getter
    private final String command;

    protected AbstractExecutor(@NonNull final InternalVentaContext context, @NonNull final String command, final List<? extends AbstractExecutor> executors) {
        this.executors = TransformationUtil.toMap(executors);
        this.internalContext = context;
        this.command = command;
    }

    protected AbstractExecutor(@NonNull final InternalVentaContext context, @NonNull final String command) {
        this(context, command, null);
    }

    protected final VentaContext getContext() {
        return internalContext.getContext();
    }

    protected final VentaState getState() {
        return internalContext.getState();
    }

    protected final AbstractExecutor getExecutor(@NonNull final String command) {
        return executors.get(command);
    }

    protected final void delegateExecution(final ConsoleQueue.Command command) {
        final var executor = getExecutor(command.getCommand());
        if (executor == null) {
            log.warn("Executor is not registered for command: {}", command.getCommand());
            return;
        }

        executor.execute(command.getSubcommand());
    }

    public abstract void execute(final ConsoleQueue.Command command);
}
