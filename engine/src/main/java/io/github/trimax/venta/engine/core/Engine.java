package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.ErrorCallback;
import io.github.trimax.venta.engine.console.ConsoleCommandExecutor;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.exceptions.EngineInitializationException;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.renderers.common.EngineRenderer;
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
    private final ConsoleCommandExecutor consoleCommandExecutor;
    private final ControllerFactory controllerFactory;
    private final EngineRenderer engineRenderer;
    private final VentaContext context;
    private final Memory memory;

    public void initialize(@NonNull final VentaEngineApplication ventaEngineApplication) {
        glfwSetErrorCallback(new ErrorCallback());
        if (!glfwInit())
            throw new EngineInitializationException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        controllerFactory.get(EngineController.class).initialize(ventaEngineApplication);
        controllerFactory.get(WindowController.class).initialize(ventaEngineApplication);
        GL.createCapabilities();

        controllerFactory.get(ConsoleController.class).initialize();
        controllerFactory.get(TextController.class).initialize();

        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
    }

    @Override
    public void run() {
        controllerFactory.get(ConsoleController.class).info("Venta engine started");
        glEnable(GL_DEPTH_TEST);

        final var engineController = controllerFactory.get(EngineController.class);
        final var updateHandler = engineController.get().getApplication().getUpdateHandler();
        final var fpsCounter = new FPSCounter();
        final var time = new VentaTime();

        while (engineController.get().isApplicationRunning()) {
            engineRenderer.render(fpsCounter);

            time.setDelta(fpsCounter.tick());
            updateHandler.onUpdate(time, context);

            consoleCommandExecutor.execute();
        }

        controllerFactory.cleanup();
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

