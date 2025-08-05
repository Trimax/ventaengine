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
public final class CameraInfoExecutor extends AbstractCameraExecutor {
    private CameraInfoExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "info", "prints information about selected camera");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var camera = getManager(CameraManagerImplementation.class).getCurrent();
        if (camera == null) {
            getConsole().error("The camera is not selected");
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
