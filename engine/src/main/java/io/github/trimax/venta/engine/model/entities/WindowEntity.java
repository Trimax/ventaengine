package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.callbacks.*;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.model.view.WindowView;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.lwjgl.glfw.*;

@Slf4j
@Getter
public final class WindowEntity extends AbstractEntity implements WindowView {
    private final GLFWFramebufferSizeCallback windowSizeCallback;
    private final GLFWMouseButtonCallback mouseClickCallback;
    private final GLFWCursorPosCallback mouseCursorCallback;
    private final GLFWCharCallback charCallback;
    private final GLFWKeyCallback keyCallback;

    private final VentaEngineInputHandler handler;
    private final ConsoleQueue consoleQueue;
    private final Matrix4f projectionMatrix;
    private final long internalID;

    @Setter
    private ConsoleEntity console;

    @Setter
    private int width;

    @Setter
    private int height;


    public WindowEntity(final long internalID,
                        final int width,
                        final int height,
                        @NonNull final String title,
                        @NonNull final VentaEngineInputHandler inputHandler,
                        @NonNull final ConsoleQueue consoleQueue) {
        super(title);

        this.handler = inputHandler;
        this.consoleQueue = consoleQueue;

        this.mouseCursorCallback = new MouseCursorCallback(this);
        this.mouseClickCallback = new MouseButtonCallback(this);
        this.windowSizeCallback = new WindowSizeCallback(this);
        this.charCallback = new KeyboardCharCallback(this);
        this.keyCallback = new KeyboardKeyCallback(this);

        this.internalID = internalID;
        this.width = width;
        this.height = height;

        final float aspectRatio = (float) width / height;
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f);
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public boolean hasConsole() {
        return console != null;
    }
}
