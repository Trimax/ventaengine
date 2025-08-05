package io.github.trimax.venta.engine.executors.system;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SystemInfoExecutor extends AbstractSystemExecutor {
    private SystemInfoExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "info", "prints information about the OS");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().info("   Name: %s", System.getProperty("os.name"));
        getConsole().info("Version: %s", System.getProperty("os.version"));
        getConsole().info("   Arch: %s", System.getProperty("os.arch"));
    }
}
