package io.github.trimax.venta.engine.console;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.executors.core.AbstractCoreExecutor;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public final class ConsoleExecutor {
    private final Map<String, AbstractCoreExecutor> executors;
    private final WindowManagerImplementation.WindowAccessor windowAccessor;
    private final WindowManagerImplementation windowManager;
    private final ConsoleQueue queue;

    private ConsoleExecutor(final List<AbstractCoreExecutor> executors, final ConsoleQueue queue,
                            final WindowManagerImplementation windowManager, final WindowManagerImplementation.WindowAccessor windowAccessor) {
        this.executors = TransformationUtil.toMap(executors, AbstractExecutor::getCommand);
        this.windowAccessor = windowAccessor;
        this.windowManager = windowManager;
        this.queue = queue;
    }

    public void execute() {
        while (queue.hasCommands())
            Optional.ofNullable(queue.poll()).ifPresent(this::execute);
    }

    private void execute(final ConsoleQueue.Command command) {
        if ("help".equalsIgnoreCase(command.getCommand())) {
            printHelp(windowAccessor.get(windowManager.getCurrent()).getConsole());
            return;
        }

        final var executor = executors.get(command.getCommand());
        if (executor == null) {
            windowAccessor.get(windowManager.getCurrent()).getConsole()
                    .error(String.format("Unknown command '%s'. Type help", command.getCommand()));
            log.warn("Executor is not registered for command: {}", command.getCommand());
            return;
        }

        executor.execute(command.getSubcommand());
        log.info("{} executed", command);
    }

    private void printHelp(final ConsoleManagerImplementation.ConsoleEntity console) {
        console.info("Available commands:");
        StreamEx.of(executors.values())
                .map(AbstractExecutor::getPublicDescription)
                .forEach(console::info);
    }
}
