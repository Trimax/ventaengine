package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleExecutor;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.context.ManagerContext;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.exceptions.EngineInitializationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.model.view.WindowView;
import io.github.trimax.venta.engine.renderers.EngineRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
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

    public void initialize(@NonNull final VentaEngineApplication ventaEngineApplication) {
        internalVentaContext.getState().setApplication(ventaEngineApplication);

        org.lwjgl.glfw.GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(final int error, final long description) {
                log.error("GLFW Error [{}]: {}", error, GLFWErrorCallback.getDescription(description));
            }
        });

        if (!glfwInit())
            throw new EngineInitializationException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        managerContext.get(WindowManagerImplementation.class).setCurrent(createWindow(ventaEngineApplication));

        GL.createCapabilities();
        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
    }

    private WindowView createWindow(final VentaEngineApplication ventaEngineApplication) {
        return managerContext.get(WindowManagerImplementation.class)
                .create(ventaEngineApplication.getConfiguration().getWindowConfiguration(),
                        ventaEngineApplication.getInputHandler());
    }

    @Override
    public void run() {
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

