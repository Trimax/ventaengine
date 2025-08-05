package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.ErrorCallback;
import io.github.trimax.venta.engine.console.ConsoleCommandExecutor;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.context.ManagerContext;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.exceptions.EngineInitializationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.renderers.EngineRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.glEnable;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Engine implements Runnable {
    private final ConsoleController consoleController;
    private final WindowController windowController;
    private final TextController textController;

    private final InternalVentaContext internalVentaContext;
    private final ConsoleCommandExecutor consoleCommandExecutor;
    private final EngineRenderer engineRenderer;
    private final ManagerContext managerContext;
    private final VentaContext context;
    private final Memory memory;

    public void initialize(@NonNull final VentaEngineApplication ventaEngineApplication) {
        internalVentaContext.getState().setApplication(ventaEngineApplication);
        glfwSetErrorCallback(new ErrorCallback());
        if (!glfwInit())
            throw new EngineInitializationException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        internalVentaContext.getState().setDebugEnabled(ventaEngineApplication.getConfiguration().getRenderConfiguration().isDebugEnabled());

        windowController.initialize(ventaEngineApplication);
        GL.createCapabilities();

        consoleController.initialize();
        textController.initialize();

        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
    }

    @Override
    public void run() {
        consoleController.info("Venta engine started");
        glEnable(GL_DEPTH_TEST);

        final var updateHandler = internalVentaContext.getState().getApplication().getUpdateHandler();
        final var fpsCounter = new FPSCounter();
        final var time = new VentaTime();

        while (internalVentaContext.getState().isApplicationRunning()) {
            engineRenderer.render(internalVentaContext.getState(), fpsCounter);

            time.setDelta(fpsCounter.tick());
            updateHandler.onUpdate(time, context);

            consoleCommandExecutor.execute();
        }

        consoleController.deinitialize();
        windowController.deinitialize();
        textController.deinitialize();

        managerContext.cleanup();
        memory.cleanup();
        glfwTerminate();
    }

    @Getter
    public static final class VentaTime {
        private double timeElapsed = 0.0;
        private double delta;

        private void setDelta(final double delta) {
            this.timeElapsed += delta;
            this.delta = delta;
        }
    }
}

