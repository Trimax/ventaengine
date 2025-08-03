package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.ErrorCallback;
import io.github.trimax.venta.engine.console.ConsoleExecutor;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.context.ManagerContext;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.exceptions.EngineInitializationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.view.WindowView;
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
    private final InternalVentaContext internalVentaContext;
    private final ConsoleExecutor consoleExecutor;
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
        managerContext.get(WindowManagerImplementation.class).setCurrent(createWindow(ventaEngineApplication));

        GL.createCapabilities();
        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
        managerContext.get(WindowManagerImplementation.class).getCurrent()
                .setConsole(managerContext.get(ConsoleManagerImplementation.class).create("Default console"));
    }

    private WindowView createWindow(final VentaEngineApplication ventaEngineApplication) {
        return managerContext.get(WindowManagerImplementation.class)
                .create(ventaEngineApplication.getConfiguration().getWindowConfiguration(),
                        ventaEngineApplication.getInputHandler());
    }

    @Override
    public void run() {
        internalVentaContext.getConsole().info("Venta engine started");
        internalVentaContext.getConsole().info("");
        glEnable(GL_DEPTH_TEST);

        final var updateHandler = internalVentaContext.getState().getApplication().getUpdateHandler();
        final var fpsCounter = new FPSCounter();
        final var time = new VentaTime();

        while (internalVentaContext.getState().isApplicationRunning()) {
            engineRenderer.render(internalVentaContext.getState(), fpsCounter);

            time.setDelta(fpsCounter.tick());
            updateHandler.onUpdate(time, context);

            consoleExecutor.execute();
        }

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

