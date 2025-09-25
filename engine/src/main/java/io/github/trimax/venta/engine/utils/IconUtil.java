package io.github.trimax.venta.engine.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.stb.STBImage.*;

@UtilityClass
public final class IconUtil {
    public void setIcon(final long windowID, @NonNull final byte[] data) {
        final var imageBuffer = BufferUtils.createByteBuffer(data.length);
        imageBuffer.put(data);
        imageBuffer.flip();

        try (final var stack = MemoryStack.stackPush()) {
            final var width = stack.mallocInt(1);
            final var height = stack.mallocInt(1);
            final var channels = stack.mallocInt(1);

            final var icon = stbi_load_from_memory(imageBuffer, width, height, channels, 0);
            if (icon == null)
                throw new RuntimeException("Failed to load icon: " + stbi_failure_reason());

            final var image = GLFWImage.malloc(stack);
            image.set(width.get(0), height.get(0), icon);

            final var icons = GLFWImage.malloc(1, stack);
            icons.put(0, image);

            glfwSetWindowIcon(windowID, icons);

            stbi_image_free(icon);
        } finally {
            MemoryUtil.memFree(imageBuffer);
        }
    }
}
