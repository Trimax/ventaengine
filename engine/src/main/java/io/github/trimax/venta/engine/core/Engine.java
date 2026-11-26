package io.github.trimax.venta.engine.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.glEnable;

import org.lwjgl.opengl.GL;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.callbacks.ErrorCallback;
import io.github.trimax.venta.engine.console.ConsoleCommandExecutor;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.controllers.AudioController;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import io.github.trimax.venta.engine.exceptions.EngineInitializationException;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineFrameListener;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import io.github.trimax.venta.engine.renderers.common.EngineRenderer;
import io.github.trimax.venta.engine.updaters.EngineUpdater;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Engine {
    private final ConsoleCommandExecutor consoleCommandExecutor;
    private final ControllerFactory controllerFactory;
    private final EngineRenderer engineRenderer;
    private final EngineUpdater engineUpdater;
    private final VentaContext context;

    /**
     * The frames of an engine that is a part of another application (see {@link #startEmbedded}).
     */
    private FPSCounter embeddedFpsCounter;
    private VentaTime embeddedTime;

    public void start(final String[] args, @NonNull final VentaEngineApplication application) {
        try {
            initialize(application);
            application.getStartupHandler().onStartup(args, context);

            run();
        } finally {
            release();
        }
    }

    /**
     * Starts the engine as a part of another application (an editor): the window is hidden, there is no loop, and the host draws the
     * frames one by one with {@link #renderFrame()} on the thread that called this method. The application must say that it is
     * embedded, see {@link io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration#isEmbedded()}.
     *
     * @param frameListener gets every finished frame before it is shown, it can read the image
     */
    public void startEmbedded(final String[] args, @NonNull final VentaEngineApplication application, final VentaEngineFrameListener frameListener) {
        if (!application.getConfiguration().isEmbedded())
            throw new EngineInitializationException("The application must be embedded to be started by a host");

        try {
            initialize(application);
            engineRenderer.setFrameListener(frameListener);
            application.getStartupHandler().onStartup(args, context);

            glEnable(GL_DEPTH_TEST);
            embeddedFpsCounter = new FPSCounter();
            embeddedTime = new VentaTime();
        } catch (final RuntimeException exception) {
            release();
            throw exception;
        }
    }

    /** Draws a frame of an embedded engine and updates the scene */
    public void renderFrame() {
        if (embeddedTime == null)
            throw new IllegalStateException("The embedded engine is not started");

        step(embeddedFpsCounter, embeddedTime);
    }

    /** The size of the window of an embedded engine, which is the size of its frames in pixels */
    public void resize(final int width, final int height) {
        glfwSetWindowSize(controllerFactory.get(WindowController.class).get().getInternalID(), width, height);
    }

    /** Stops an embedded engine and releases everything it owns */
    public void shutdown() {
        embeddedTime = null;
        embeddedFpsCounter = null;
        release();
    }

    /** The frame rate of the last second of an embedded engine */
    public double getFrameRate() {
        return embeddedFpsCounter == null ? 0.0 : embeddedFpsCounter.getCurrentFps();
    }

    private void release() {
        // The renderers own GL objects, the context is destroyed by the controllers
        engineRenderer.release();
        controllerFactory.cleanup();
        glfwTerminate();

        final var errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null)
            errorCallback.free();
    }

    private void initialize(@NonNull final VentaEngineApplication ventaEngineApplication) {
        glfwSetErrorCallback(new ErrorCallback());

        // The application that hosts the engine has its own menu bar
        if (ventaEngineApplication.getConfiguration().isEmbedded())
            glfwInitHint(GLFW_COCOA_MENUBAR, GLFW_FALSE);

        if (!glfwInit())
            throw new EngineInitializationException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        controllerFactory.get(AudioController.class).initialize();
        controllerFactory.get(KeyboardController.class).initialize();
        controllerFactory.get(EngineController.class).initialize(ventaEngineApplication);
        controllerFactory.get(WindowController.class).initialize(ventaEngineApplication);
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);

        controllerFactory.get(ConsoleController.class).initialize();
        controllerFactory.get(TextController.class).initialize();

        context.getCameraManager().setCurrent(context.getCameraManager().create("Default camera"));
        context.getSceneManager().setCurrent(context.getSceneManager().create("Default scene"));
    }

    private void run() {
        controllerFactory.get(ConsoleController.class).info("Venta engine started");
        glEnable(GL_DEPTH_TEST);

        final var engineController = controllerFactory.get(EngineController.class);
        final var updateHandler = engineController.get().getApplication().getUpdateHandler();
        final var fpsCounter = new FPSCounter();
        final var time = new VentaTime();

        while (engineController.get().isApplicationRunning())
            step(fpsCounter, time, updateHandler);
    }

    private void step(final FPSCounter fpsCounter, final VentaTime time) {
        step(fpsCounter, time, controllerFactory.get(EngineController.class).get().getApplication().getUpdateHandler());
    }

    private void step(final FPSCounter fpsCounter, final VentaTime time, final VentaEngineUpdateHandler updateHandler) {
        engineRenderer.render(fpsCounter, time);

        time.setDelta(fpsCounter.tick());
        updateHandler.onUpdate(time, context);
        engineUpdater.update(time);

        consoleCommandExecutor.execute();
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

