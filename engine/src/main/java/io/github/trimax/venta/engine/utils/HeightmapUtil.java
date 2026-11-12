package io.github.trimax.venta.engine.utils;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.util.function.IntToDoubleFunction;

import org.lwjgl.stb.STBImage;

import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;
import io.github.trimax.venta.engine.model.common.terrain.Heightmap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class HeightmapUtil {
    /**
     * Decodes an image file into heightmap values. The image is decoded the same way as a texture (16-bit images keep
     * their precision, the others have 8 bits per channel) and the first channel is used, because the terrain shader
     * reads the red channel.
     *
     * @param name image name for error messages
     * @param data image file content, its position and limit are kept
     */
    public Heightmap decode(@NonNull final String name, @NonNull final ByteBuffer data) {
        try (var stack = stackPush()) {
            final var widthBuffer = stack.mallocInt(1);
            final var heightBuffer = stack.mallocInt(1);
            final var channelsBuffer = stack.mallocInt(1);

            if (STBImage.stbi_is_16_bit_from_memory(data)) {
                final var pixels = STBImage.stbi_load_16_from_memory(data, widthBuffer, heightBuffer, channelsBuffer, 0);
                if (pixels == null)
                    throw new UnknownTextureFormatException(String.format("%s (%s)", name, STBImage.stbi_failure_reason()));

                try {
                    final var channels = channelsBuffer.get(0);
                    return create(widthBuffer.get(0), heightBuffer.get(0), i -> (pixels.get(i * channels) & 0xFFFF) / 65535.f);
                } finally {
                    STBImage.stbi_image_free(pixels);
                }
            }

            final var pixels = STBImage.stbi_load_from_memory(data, widthBuffer, heightBuffer, channelsBuffer, 0);
            if (pixels == null)
                throw new UnknownTextureFormatException(String.format("%s (%s)", name, STBImage.stbi_failure_reason()));

            try {
                final var channels = channelsBuffer.get(0);
                return create(widthBuffer.get(0), heightBuffer.get(0), i -> (pixels.get(i * channels) & 0xFF) / 255.f);
            } finally {
                STBImage.stbi_image_free(pixels);
            }
        }
    }

    private Heightmap create(final int width, final int height, @NonNull final IntToDoubleFunction texel) {
        final var values = new float[width * height];
        for (int i = 0; i < values.length; i++)
            values[i] = (float) texel.applyAsDouble(i);

        return new Heightmap(width, height, values);
    }
}
