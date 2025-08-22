package io.github.trimax.venta.engine.executors;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;
import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import io.github.trimax.venta.engine.model.states.EngineState;
import io.github.trimax.venta.engine.registries.implementation.AbstractRegistryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.AbstractRepositoryImplementation;
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
    private final ConsoleController consoleController;
    private final EngineController engineController;

    @Getter
    @Accessors(makeFinal = true)
    private final String command;
    private final String description;

    protected AbstractExecutor(@NonNull final ControllerFactory factory,
                               @NonNull final String command,
                               @NonNull final String description,
                               final List<? extends AbstractExecutor> executors) {
        this.executors = TransformationUtil.toMap(executors, AbstractExecutor::getCommand);
        this.consoleController = factory.get(ConsoleController.class);
        this.engineController = factory.get(EngineController.class);
        this.description = description;
        this.command = command;
    }

    protected AbstractExecutor(@NonNull final ControllerFactory factory,
                               @NonNull final String command,
                               @NonNull final String description) {
        this(factory, command, description, null);
    }

    public <T extends I, I extends AbstractInstance, M extends AbstractManagerImplementation<T, I>> M getManager(@NonNull final Class<M> managerClass) {
        return engineController.getManager(managerClass);
    }


    public <A, T extends E, E extends AbstractEntity, R extends AbstractRegistryImplementation<T, E, A>> R getRegistry(@NonNull final Class<R> registryClass) {
        return engineController.getRegistry(registryClass);
    }

    public <T extends P, P extends AbstractPrefab, R extends AbstractRepositoryImplementation<T, P>> R getRepository(@NonNull final Class<R> repositoryClass) {
        return engineController.getRepository(repositoryClass);
    }

    protected final EngineState getState() {
        return engineController.get();
    }

    protected final ConsoleController getConsole() {
        return consoleController;
    }

    protected final AbstractExecutor getExecutor(@NonNull final String command) {
        return executors.get(command);
    }

    protected final void delegateExecution(final ConsoleCommandQueue.Command command) {
        if (StringUtils.isBlank(command.getCommand())) {
            getConsole().warning(String.format("The command is missing. Type '%s help' to see the options", getCommand()));
            return;
        }

        if ("help".equalsIgnoreCase(command.getCommand())) {
            printHelp();
            return;
        }

        final var executor = getExecutor(command.getCommand());
        if (executor == null) {
            getConsole().error(String.format("Unknown command: '%s'. Type '%s help'",
                    command.getFullPath(), command.parent().getFullPath()));
            log.warn("Executor is not registered for command: {}", command.parent().getFullPath());
            return;
        }

        executor.execute(command.getSubcommand());
    }

    public String getPublicDescription() {
        return String.format("  %-20s - %s", command, description);
    }

    private void printHelp() {
        getConsole().info("Available commands:");
        StreamEx.of(executors.values())
                .map(AbstractExecutor::getPublicDescription)
                .forEach(getConsole()::info);
    }

    public abstract void execute(final ConsoleCommandQueue.Command command);
}
