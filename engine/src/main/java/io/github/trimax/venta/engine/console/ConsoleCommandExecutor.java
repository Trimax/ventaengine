package io.github.trimax.venta.engine.console;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.executors.AbstractExecutor;
import io.github.trimax.venta.engine.executors.core.AbstractCoreExecutor;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public final class ConsoleCommandExecutor {
    private final Map<String, AbstractCoreExecutor> executors;
    private final ConsoleController consoleController;
    private final ConsoleCommandQueue queue;

    private ConsoleCommandExecutor(final List<AbstractCoreExecutor> executors, final ConsoleCommandQueue queue,
                                   final ConsoleController consoleController) {
        this.executors = TransformationUtil.toMap(executors, AbstractExecutor::getCommand);
        this.consoleController = consoleController;
        this.queue = queue;
    }

    public void execute() {
        while (queue.hasCommands())
            Optional.ofNullable(queue.poll()).ifPresent(this::execute);
    }

    private void execute(final ConsoleCommandQueue.Command command) {
        consoleController.emptyLine();
        consoleController.debug("> %s", command.value());

        if ("help".equalsIgnoreCase(command.getCommand())) {
            printHelp();
            return;
        }

        if ("exit".equalsIgnoreCase(command.getCommand()) || "quit".equalsIgnoreCase(command.getCommand())) {
            consoleController.warning("You probably meant 'engine stop'");
            return;
        }

        final var executor = executors.get(command.getCommand());
        if (executor == null) {
            consoleController.error(String.format("Unknown command '%s'. Type help", command.getCommand()));
            log.warn("Executor is not registered for command: {}", command.getCommand());
            return;
        }

        executor.execute(command.getSubcommand());
        log.info("{} executed", command);
    }

    private void printHelp() {
        consoleController.info("Available commands:");
        StreamEx.of(executors.values())
                .map(AbstractExecutor::getPublicDescription)
                .forEach(consoleController::info);
    }
}
