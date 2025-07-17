package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.WindowCreationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
@Component
public final class WindowManager extends AbstractManager<WindowManager.WindowEntity> {
    static final WindowManager instance = new WindowManager();

    public WindowEntity create(final String title, final int width, final int height) {
        log.info("Creating window: {}", title);
        final var id = glfwCreateWindow(width, height, title, NULL, NULL);
        if (id == NULL)
            throw new WindowCreationException(title);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1);
        glfwShowWindow(id);

        return store(new WindowEntity(id, width, height, title));
    }

    @Override
    protected void destroy(final WindowEntity window) {
        log.info("Destroying window {}", window.getTitle());
        glfwDestroyWindow(window.getId());
    }

    @Getter
    public static final class WindowEntity extends AbstractEntity {
        private final int width;
        private final int height;
        private final String title;

        WindowEntity(final long id, final int width, final int height, @NonNull final String title) {
            super(id);

            this.width = width;
            this.height = height;
            this.title = title;
        }
    }
}
