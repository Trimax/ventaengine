package io.github.trimax.venta.engine.executors.object;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class ObjectDescribeExecutor extends AbstractObjectExecutor {
    private ObjectDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the object");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var objectManager = getManager(ObjectManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var object = objectManager.getInstance(command.asArgument().value());
        if (object == null) {
            getConsole().error("Object <%s> can't be described because it does not exist", command.asArgument().value());
            return;
        }

        getConsole().header("Object <%s>:", object.getID());
        getConsole().info("        Name: %s", object.getName());
        getConsole().info("  Program ID: %s", object.hasProgram() ? object.getProgram().getID() : "not assigned");
        getConsole().info("    Position: %s", object.getPosition());
        getConsole().info("    Rotation: %s", object.getRotation());
        getConsole().info("       Scale: %s", object.getScale());
        getConsole().info("   Draw mode: %s", object.getDrawMode());
        getConsole().info("     Visible: %b", object.isVisible());
        getConsole().info("         Lit: %b", object.isLit());
    }
}
