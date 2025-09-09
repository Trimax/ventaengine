package io.github.trimax.packer.util;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.FileOutputStream;
import java.io.IOException;

import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.packer.model.Image;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ImageUtil {
    public Image read(@NonNull final String path) {
        try (final var stack = stackPush()) {
            final var w = stack.mallocInt(1);
            final var h = stack.mallocInt(1);
            final var c = stack.mallocInt(1);

            final var pixels = STBImage.stbi_load(path, w, h, c, 0);
            if (pixels == null)
                throw new RuntimeException("Failed to load image (" + path + "): " + STBImage.stbi_failure_reason());

            final var width = w.get(0);
            final var height = h.get(0);
            final var channels = c.get(0);

            final var pixelArray = new byte[width * height * channels];
            pixels.duplicate().get(pixelArray);
            STBImage.stbi_image_free(pixels);

            return new Image(w.get(0), h.get(0), c.get(0), pixelArray);
        }
    }

    public void write(@NonNull final String path, @NonNull final Image image) {
        try (var out = new FileOutputStream(path)) {
            final var buffer = MemoryUtil.memAlloc(image.data().length);
            buffer.put(image.data()).flip();

            try (final var callback = STBIWriteCallback.create((_, dataPointer, size) -> {
                final var chunkBuffer = MemoryUtil.memByteBuffer(dataPointer, size);
                final var chunk = new byte[size];
                chunkBuffer.get(chunk);
                try {
                    out.write(chunk);
                } catch (final IOException e) {
                    throw new RuntimeException("Failed to write image: " + path);
                }
            })) {
                if (!STBImageWrite.stbi_write_png_to_func(
                        callback,
                        0,
                        image.width(),
                        image.height(),
                        image.channels(),
                        buffer,
                        image.width() * image.channels()))
                    throw new RuntimeException("Failed to write image: " + path);

            } finally {
                MemoryUtil.memFree(buffer);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write image: " + path, e);
        }
    }
}
