package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;
import io.github.trimax.venta.engine.exceptions.WindowCreationException;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.managers.WindowManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.WindowEntity;
import io.github.trimax.venta.engine.model.view.WindowView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowManagerImplementation
        extends AbstractManagerImplementation<WindowEntity, WindowView>
        implements WindowManager {
    private final ConsoleCommandQueue consoleCommandQueue;
    private final Memory memory;

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

    private WindowEntity create(@NonNull final String title,
                                final long monitorID,
                                final int width,
                                final int height,
                                final VentaEngineInputHandler handler) {
        log.info("Creating window: {}", title);

        final var id = memory.getWindows().create(() -> glfwCreateWindow(width, height, title, monitorID, NULL), "Window %s", title);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)
        glfwShowWindow(id);
        glfwRestoreWindow(id);
        glfwFocusWindow(id);

        final var window = new WindowEntity(id, width, height, title, handler, consoleCommandQueue);
        glfwSetFramebufferSizeCallback(id, window.getWindowSizeCallback());
        glfwSetMouseButtonCallback(id, window.getMouseClickCallback());
        glfwSetCursorPosCallback(id, window.getMouseCursorCallback());
        glfwSetCharCallback(id, window.getCharCallback());
        glfwSetKeyCallback(id, window.getKeyCallback());

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
        window.getCharCallback().close();
        window.getKeyCallback().close();
        window.getWindowSizeCallback().close();
        window.getMouseClickCallback().close();
        window.getMouseCursorCallback().close();
        memory.getWindows().delete(window.getInternalID());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
