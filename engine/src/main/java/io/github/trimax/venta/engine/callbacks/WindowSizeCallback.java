package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.model.entities.WindowEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import static org.lwjgl.opengl.GL11C.glViewport;

@Slf4j
@AllArgsConstructor
public final class WindowSizeCallback extends GLFWFramebufferSizeCallback implements AbstractCallback {
    private final WindowEntity window;

    @Override
    public void invoke(final long windowID, final int width, final int height) {
        window.setWidth(width);
        window.setHeight(height);

        glViewport(0, 0, width, height);
        log.info("Window resized: {}x{}", width, height);

        final float aspectRatio = (float) width / height;
        window.getProjectionMatrix().set(new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f));
    }
}
