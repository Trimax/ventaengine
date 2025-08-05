package io.github.trimax.venta.engine.executors.camera;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class CameraDescribeExecutor extends AbstractCameraExecutor {
    private CameraDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the camera");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var cameraManager = getManager(CameraManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var camera = cameraManager.getInstance(command.asArgument().value());
        if (camera == null) {
            getConsole().error("Camera <%s> can't be described because it does not exist", command.asArgument().value());
            return;
        }

        getConsole().header("Camera <%s>:", camera.getID());
        getConsole().info("         Name: %s", camera.getName());
        getConsole().info("     Position: %s", camera.getPosition());
        getConsole().info("     Rotation: %s", camera.getRotation());
        getConsole().info("        Front: %s", camera.getFront());
        getConsole().info("           Up: %s", camera.getUp());
        getConsole().info("        Right: %b", camera.getRight());
    }
}
