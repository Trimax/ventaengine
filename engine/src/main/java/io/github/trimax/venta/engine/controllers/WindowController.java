package io.github.trimax.venta.engine.controllers;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.KeyboardCharCallback;
import io.github.trimax.venta.engine.callbacks.KeyboardKeyCallback;
import io.github.trimax.venta.engine.callbacks.MouseButtonCallback;
import io.github.trimax.venta.engine.callbacks.MouseCursorCallback;
import io.github.trimax.venta.engine.callbacks.WindowSizeCallback;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.exceptions.WindowCreationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.states.WindowState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowController extends AbstractController<WindowState, VentaEngineApplication> {
    private final ConsoleCommandQueue consoleCommandQueue;
    private final KeyboardController keyboardController;
    private final ConsoleController consoleController;
    private final Memory memory;

    @Override
    protected WindowState create(@NonNull final VentaEngineApplication application) {
        final var windowConfiguration = application.getConfiguration().getWindowConfiguration();

        if (!windowConfiguration.isFullscreen())
            return create(windowConfiguration.title(), NULL, windowConfiguration.width(), windowConfiguration.height(),
                    application.getInputHandler(), application.getConfiguration().getRenderConfiguration());

        final var monitorID = glfwGetPrimaryMonitor();
        if (monitorID == NULL)
            throw new WindowCreationException("Can't determine primary monitor");

        final var videoMode = glfwGetVideoMode(monitorID);
        if (videoMode == null)
            throw new WindowCreationException("Can't determine video mode for the fullscreen window");

        return create(windowConfiguration.title(), monitorID, videoMode.width(), videoMode.height(),
                application.getInputHandler(), application.getConfiguration().getRenderConfiguration());
    }

    private WindowState create(@NonNull final String title,
                                final long monitorID,
                                final int width,
                                final int height,
                                final VentaEngineInputHandler handler,
                                final VentaEngineConfiguration.RenderConfiguration renderConfiguration) {
        log.info("Initializing window");

        glfwWindowHint(GLFW_SAMPLES, renderConfiguration.antialiasingSamples().getValue());
        final var id = memory.getWindows().create(() -> glfwCreateWindow(width, height, title, monitorID, NULL), "Window %s", title);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(renderConfiguration.isVerticalSynchronizationEnabled() ? 1 : 0);
        glfwShowWindow(id);
        glfwRestoreWindow(id);
        glfwFocusWindow(id);

        final var window = new WindowState(handler, id, title, width, height);
        glfwSetFramebufferSizeCallback(id, new WindowSizeCallback(this));
        glfwSetMouseButtonCallback(id, new MouseButtonCallback(this));
        glfwSetCursorPosCallback(id, new MouseCursorCallback(this));
        glfwSetCharCallback(id, new KeyboardCharCallback(consoleController));
        glfwSetKeyCallback(id, new KeyboardKeyCallback(consoleCommandQueue, keyboardController, consoleController, this));

        glfwMakeContextCurrent(id);

        if (!renderConfiguration.isMouseCursorVisible())
            glfwSetInputMode(id, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        return window;
    }

    @Override
    protected void destroy(@NonNull final WindowState state) {
        log.info("Deinitializing window");

        glfwMakeContextCurrent(NULL);
        memory.getWindows().delete(state.getInternalID());
    }
}
