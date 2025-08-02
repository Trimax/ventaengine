package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;
import io.github.trimax.venta.engine.exceptions.WindowCreationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.managers.WindowManager;
import io.github.trimax.venta.engine.model.view.WindowView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowManagerImplementation
        extends AbstractManagerImplementation<WindowManagerImplementation.WindowEntity, WindowView>
        implements WindowManager {
    private final ConsoleQueue consoleQueue;

    @Getter
    private WindowEntity current;

    public WindowEntity create(final VentaEngineConfiguration.WindowConfiguration configuration, final VentaEngineInputHandler inputHandler) {
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

    private WindowEntity create(final String title, final long monitorID, final int width, final int height, final VentaEngineInputHandler inputHandler) {
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
        glfwSetCharCallback(id, window.getCharCallback());
        glfwSetMouseButtonCallback(id, window.getMouseButtonCallback());
        glfwSetCursorPosCallback(id, window.getMousePositionCallback());

        setIconFromResources(id, "/icons/venta.png");

        return store(window);
    }

    public void setCurrent(@NonNull final WindowView window) {
        if (window instanceof WindowEntity entity) {
            this.current = entity;
            glfwMakeContextCurrent(getEntity(window.getID()).getInternalID());
        }
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

            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer comp = stack.mallocInt(1);

            final ByteBuffer iconPixels = STBImage.stbi_load_from_memory(imageBuffer, width, height, comp, 4);
            if (iconPixels == null) {
                MemoryUtil.memFree(imageBuffer);
                throw new UnknownTextureFormatException(String.format("%s (%s)", resourcePath, STBImage.stbi_failure_reason()));
            }

            final GLFWImage icon = GLFWImage.malloc(stack);
            icon.set(width.get(0), height.get(0), iconPixels);

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

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Window;
    }

    @Getter
    public  final class WindowEntity extends AbstractEntity implements WindowView {
        private final long internalID;
        private final VentaEngineInputHandler inputHandler;
        private final Matrix4f projectionMatrix;

        @Setter
        private ConsoleManagerImplementation.ConsoleEntity console;

        private int width;
        private int height;

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

        @Getter(AccessLevel.PRIVATE)
        private final GLFWCharCallback charCallback = new GLFWCharCallback() {
            @Override
            public void invoke(final long window, final int code) {
                if (!console.isVisible())
                    return;

                console.accept((char) code);
            }
        };

        @Getter(AccessLevel.PRIVATE)
        private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(final long window, final int key, final int scancode, final int action, final int mods) {
                if (key == GLFW_KEY_F12 && action == GLFW_PRESS) {
                    console.toggle();
                    return;
                }

                if (console.isVisible()) {
                    if (action == GLFW_PRESS || action == GLFW_REPEAT)
                        console.handle(key, consoleQueue::add);
                    return;
                }

                if (inputHandler != null)
                    inputHandler.onKey(key, scancode, action, mods);
            }
        };

        @Getter(AccessLevel.PRIVATE)
        private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(final long window, final int button, final int action, final int mods) {
                if (inputHandler != null)
                    inputHandler.onMouseButton(button, action, mods);
            }
        };

        @Getter(AccessLevel.PRIVATE)
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
}
