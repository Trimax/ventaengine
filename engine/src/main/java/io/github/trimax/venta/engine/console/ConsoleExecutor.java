package io.github.trimax.venta.engine.console;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.core.AbstractCoreExecutor;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class ConsoleExecutor {
    private final Map<String, AbstractCoreExecutor> executors;
    private final ConsoleQueue queue;

    private ConsoleExecutor(final List<AbstractCoreExecutor> executors, final ConsoleQueue queue) {
        this.executors = TransformationUtil.toMap(executors);
        this.queue = queue;
    }

    public void execute() {
        while (queue.hasCommands())
            Optional.ofNullable(queue.poll()).ifPresent(this::execute);
    }

    private void execute(final ConsoleQueue.Command command) {
        final var executor = executors.get(command.getCommand());
        if (executor == null) {
            log.warn("Executor is not registered for command: {}", command.getCommand());
            return;
        }

        executor.execute(command.getSubcommand());
        log.info("{} executed", command);
    }
}
