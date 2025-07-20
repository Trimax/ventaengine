package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.WindowCreationException;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.WindowView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class WindowManager extends AbstractManager<WindowManager.WindowEntity, WindowView> {
    @Getter
    @Setter(onParam_ = @__(@NonNull))
    private WindowView current;

    public WindowView create(final String title, final int width, final int height) {
        log.info("Creating window: {}", title);
        final var id = glfwCreateWindow(width, height, title, NULL, NULL);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1);
        glfwShowWindow(id);

        final var window = new WindowEntity(id, width, height, title);
        glfwSetFramebufferSizeCallback(id, window.getSizeCallback());

        return store(window, new WindowView(window));
    }

    public void set(@NonNull final WindowView window) {
        this.current = window;
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
