package io.github.trimax.venta.engine.executors;

import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.core.InternalVentaContext;
import io.github.trimax.venta.engine.core.VentaContext;
import io.github.trimax.venta.engine.core.VentaState;
import io.github.trimax.venta.engine.managers.ConsoleManager;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractExecutor {
    private final Map<String, ? extends AbstractExecutor> executors;
    private final InternalVentaContext internalContext;

    @Getter
    @Accessors(makeFinal = true)
    private final String command;
    private final String description;

    protected AbstractExecutor(@NonNull final InternalVentaContext context,
                               @NonNull final String command,
                               @NonNull final String description,
                               final List<? extends AbstractExecutor> executors) {
        this.executors = TransformationUtil.toMap(executors);
        this.internalContext = context;
        this.description = description;
        this.command = command;
    }

    protected AbstractExecutor(@NonNull final InternalVentaContext context,
                               @NonNull final String command,
                               @NonNull final String description) {
        this(context, command, description, null);
    }

    protected final VentaContext getContext() {
        return internalContext.getContext();
    }

    protected final ConsoleManager.ConsoleEntity getConsole() {
        return internalContext.getConsole();
    }

    protected final VentaState getState() {
        return internalContext.getState();
    }

    protected final AbstractExecutor getExecutor(@NonNull final String command) {
        return executors.get(command);
    }

    protected final void delegateExecution(final ConsoleQueue.Command command) {
        if (StringUtils.isBlank(command.getCommand())) {
            getConsole().warning(String.format("The command is missing. Type '%s help' to see the options", getCommand()));
            return;
        }

        if ("help".equalsIgnoreCase(command.getCommand())) {
            printHelp(internalContext.getConsole());
            return;
        }

        final var executor = getExecutor(command.getCommand());
        if (executor == null) {
            internalContext.getConsole().error(String.format("Unknown command: '%s'. Type '%s help'", command.getCommand(), getCommand()));
            log.warn("Executor is not registered for command: {}", command.getCommand());
            return;
        }

        executor.execute(command.getSubcommand());
    }

    public String getPublicDescription() {
        return String.format("  %-20s - %s", command, description);
    }

    private void printHelp(final ConsoleManager.ConsoleEntity console) {
        console.info("Available commands:");
        StreamEx.of(executors.values())
                .map(AbstractExecutor::getPublicDescription)
                .forEach(console::info);
    }

    public abstract void execute(final ConsoleQueue.Command command);
}
