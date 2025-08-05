package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.*;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.exceptions.WindowCreationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.WindowEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowController extends AbstractController<WindowEntity, VentaEngineApplication> {
    private final ConsoleCommandQueue consoleCommandQueue;
    private final ConsoleController consoleController;
    private final Memory memory;

    @Override
    protected WindowEntity create(@NonNull final VentaEngineApplication application) {
        final var windowConfiguration = application.getConfiguration().getWindowConfiguration();

        if (!windowConfiguration.isFullscreen())
            return create(windowConfiguration.title(), NULL, windowConfiguration.width(), windowConfiguration.height(), application.getInputHandler());

        final var monitorID = glfwGetPrimaryMonitor();
        if (monitorID == NULL)
            throw new WindowCreationException("Can't determine primary monitor");

        final var videoMode = glfwGetVideoMode(monitorID);
        if (videoMode == null)
            throw new WindowCreationException("Can't determine video mode for the fullscreen window");

        return create(windowConfiguration.title(), monitorID, videoMode.width(), videoMode.height(), application.getInputHandler());
    }

    private WindowEntity create(@NonNull final String title,
                                final long monitorID,
                                final int width,
                                final int height,
                                final VentaEngineInputHandler handler) {
        log.info("Creating window");

        final var id = memory.getWindows().create(() -> glfwCreateWindow(width, height, title, monitorID, NULL), "Window %s", title);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)
        glfwShowWindow(id);
        glfwRestoreWindow(id);
        glfwFocusWindow(id);

        final var window = new WindowEntity(id, width, height, title, handler);
        glfwSetFramebufferSizeCallback(id, new WindowSizeCallback(this));
        glfwSetMouseButtonCallback(id, new MouseButtonCallback(this));
        glfwSetCursorPosCallback(id, new MouseCursorCallback(this));
        glfwSetCharCallback(id, new KeyboardCharCallback(consoleController));
        glfwSetKeyCallback(id, new KeyboardKeyCallback(consoleCommandQueue, consoleController, this));

        glfwMakeContextCurrent(id);

        return window;
    }

    @Override
    protected void destroy(@NonNull final WindowEntity window) {
        log.info("Destroying window");

        glfwMakeContextCurrent(NULL);
        memory.getWindows().delete(window.getInternalID());
    }
}
