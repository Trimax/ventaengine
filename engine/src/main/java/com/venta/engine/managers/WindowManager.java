package com.venta.engine.managers;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.WindowCreationException;
import com.venta.engine.interfaces.VentaEngineConfiguration;
import com.venta.engine.interfaces.VentaEngineInputHandler;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowManager extends AbstractManager<WindowManager.WindowEntity, WindowView> {
    @Getter
    private WindowView current;

    public WindowView create(final VentaEngineConfiguration.WindowConfiguration configuration, final VentaEngineInputHandler inputHandler) {
        if (!configuration.isFullscreen())
            return create(configuration.title(), NULL, configuration.width(), configuration.height(), inputHandler);

        final var monitorID = glfwGetPrimaryMonitor();
        if (monitorID == NULL)
            throw new WindowCreationException("Can't determine primary monitor");

        final var videoMode = glfwGetVideoMode(monitorID);
        if (videoMode == null)
            throw new WindowCreationException("Can't determine video mode for the fullscreen window");

        return create(configuration.title(), monitorID, videoMode.width(), videoMode.height(), inputHandler);
    }

    private WindowView create(final String title, final long monitorID, final int width, final int height, final VentaEngineInputHandler inputHandler) {
        log.info("Creating window: {}", title);
        final var id = glfwCreateWindow(width, height, title, monitorID, NULL);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)
        glfwShowWindow(id);
        glfwRestoreWindow(id);
        glfwFocusWindow(id);

        final var window = new WindowEntity(id, width, height, title, inputHandler);
        glfwSetFramebufferSizeCallback(id, window.getSizeCallback());
        glfwSetKeyCallback(id, window.getKeyCallback());
        glfwSetMouseButtonCallback(id, window.getMouseButtonCallback());
        glfwSetCursorPosCallback(id, window.getMousePositionCallback());

        setIconFromResources(id, "/icons/venta.png");

        return store(window);
    }

    public void setCurrent(@NonNull final WindowView window) {
        this.current = window;
        glfwMakeContextCurrent(get(window.getID()).getInternalID());
    }

    //TODO: Reimplement it more clean (using ResourceManager)
    @SneakyThrows
    public void setIconFromResources(final long windowID, final String resourcePath) {
        try (final var stack = MemoryStack.stackPush(); final var stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null)
                throw new IllegalArgumentException("Resource not found: " + resourcePath);

            final byte[] bytes = stream.readAllBytes();
            final ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length);
            imageBuffer.put(bytes).flip();

            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer comp = stack.mallocInt(1);

            final ByteBuffer iconPixels = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (iconPixels == null) {
                MemoryUtil.memFree(imageBuffer);
                throw new RuntimeException("Failed to load icon from memory: " + STBImage.stbi_failure_reason());
            }

            final GLFWImage icon = GLFWImage.malloc(stack);
            icon.set(w.get(0), h.get(0), iconPixels);

            final GLFWImage.Buffer icons = GLFWImage.malloc(1, stack);
            icons.put(0, icon);

            if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                glfwSetWindowIcon(windowID, icons);

            STBImage.stbi_image_free(iconPixels);
            MemoryUtil.memFree(imageBuffer);
        }
    }

    @Override
    protected void destroy(final WindowEntity window) {
        log.info("Destroying window {} ({})", window.getID(), window.getName());
        window.sizeCallback.close();
        window.keyCallback.close();
        glfwDestroyWindow(window.getInternalID());
    }

    @Getter
    public static final class WindowEntity extends AbstractEntity implements WindowView {
        private final long internalID;
        private int width;
        private int height;
        private final VentaEngineInputHandler inputHandler;
        private final Matrix4f projectionMatrix;

        @Getter(AccessLevel.PRIVATE)
        private final GLFWFramebufferSizeCallback sizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(final long windowID, final int width, final int height) {
                WindowEntity.this.width = width;
                WindowEntity.this.height = height;

                glViewport(0, 0, width, height);
                log.info("Window resized: {}x{}", width, height);

                final float aspectRatio = (float) width / height;
                projectionMatrix.set(new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f));
            }
        };

        @Getter
        private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(final long window, final int key, final int scancode, final int action, final int mods) {
                if (inputHandler != null)
                    inputHandler.onKey(key, scancode, action, mods);
            }
        };

        @Getter
        private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(final long window, final int button, final int action, final int mods) {
                if (inputHandler != null)
                    inputHandler.onMouseButton(button, action, mods);
            }
        };

        @Getter
        private final GLFWCursorPosCallback mousePositionCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(final long window, final double x, final double y) {
                if (inputHandler != null)
                    inputHandler.onMouseMove(x, y);
            }
        };

        WindowEntity(final long internalID, final int width, final int height, @NonNull final String title, final VentaEngineInputHandler inputHandler) {
            super(title);

            this.internalID = internalID;
            this.width = width;
            this.height = height;
            this.inputHandler = inputHandler;

            final float aspectRatio = (float) width / height;
            projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class WindowAccessor extends AbstractAccessor {}
}
