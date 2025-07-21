package com.venta.engine.managers;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.exceptions.WindowCreationException;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.WindowView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class WindowManager extends AbstractManager<WindowManager.WindowEntity, WindowView> {
    @Getter
    private WindowView current;

    public WindowView create(final WindowConfiguration configuration) {
        if (!configuration.isFullscreen())
            return create(configuration.title(), NULL, configuration.width(), configuration.height());

        final var monitorID = glfwGetPrimaryMonitor();
        if (monitorID == NULL)
            throw new WindowCreationException("Can't determine primary monitor");

        final var videoMode = glfwGetVideoMode(monitorID);
        if (videoMode == null)
            throw new WindowCreationException("Can't determine video mode for the fullscreen window");

        return create(configuration.title(), monitorID, videoMode.width(), videoMode.height());
    }

    private WindowView create(final String title, final long monitorID, final int width, final int height) {
        log.info("Creating window: {}", title);
        final var id = glfwCreateWindow(width, height, title, monitorID, NULL);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)
        glfwShowWindow(id);
        glfwRestoreWindow(id);
        glfwFocusWindow(id);

        final var window = new WindowEntity(id, width, height, title);
        glfwSetFramebufferSizeCallback(id, window.getSizeCallback());

        setIconFromResources(id, "/icons/venta.png");

        return store(window);
    }

    public void setCurrent(@NonNull final WindowView window) {
        this.current = window;
        glfwMakeContextCurrent(get(window.getId()).entity().getId());
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
    protected WindowView createView(final String id, final WindowEntity entity) {
        return new WindowView(id, entity);
    }

    @Override
    protected void destroy(final Couple<WindowEntity, WindowView> window) {
        log.info("Deleting window {}", window.entity().getTitle());
        window.entity().sizeCallback.close();
        glfwDestroyWindow(window.entity().getId());
    }

    @Getter
    public static final class WindowEntity extends AbstractEntity {
        private int width;
        private int height;
        private final String title;

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
        private final Matrix4f projectionMatrix;

        WindowEntity(final long id, final int width, final int height, @NonNull final String title) {
            super(id);

            this.width = width;
            this.height = height;
            this.title = title;

            final float aspectRatio = (float) width / height;
            projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f);
        }
    }
}
