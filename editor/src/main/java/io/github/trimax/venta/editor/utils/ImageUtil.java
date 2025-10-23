package io.github.trimax.venta.editor.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.editor.enums.Channel;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ImageUtil {
    public BufferedImage extract(@NonNull final BufferedImage source, @NonNull final Channel channel, final int value) {
        final var result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final var hasAlpha = source.getColorModel().hasAlpha();
        final var raster = result.getRaster();

        for (int y = 0; y < source.getHeight(); y++)
            for (int x = 0; x < source.getWidth(); x++)
                raster.setSample(x, y, 0, ColorUtil.extract(source, x, y).getChannel(channel, hasAlpha, value));

        return result;
    }

    public BufferedImage mix(@NonNull final BufferedImage sourceRed,
                             @NonNull final BufferedImage sourceGreen,
                             @NonNull final BufferedImage sourceBlue,
                             @NonNull final BufferedImage sourceAlpha) {
        final int width = sourceRed.getWidth();
        final int height = sourceRed.getHeight();

        final BufferedImage destination = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final var r = sourceRed.getRaster().getSample(x, y, 0);
                final var g = sourceGreen.getRaster().getSample(x, y, 0);
                final var b = sourceBlue.getRaster().getSample(x, y, 0);
                final var a = sourceAlpha.getRaster().getSample(x, y, 0);

                final int argb = (a << 24) | (r << 16) | (g << 8) | b;
                destination.setRGB(x, y, argb);
            }
        }

        return destination;
    }

    public void write(@NonNull final File file, @NonNull final BufferedImage image) {
        try (var out = new FileOutputStream(file)) {
            final var buffer = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    final var color = ColorUtil.extract(image, x, y);

                    buffer.put((byte) color.r());
                    buffer.put((byte) color.g());
                    buffer.put((byte) color.b());
                    buffer.put((byte) color.a());
                }
            }
            buffer.flip();

            try (final var callback = STBIWriteCallback.create((_, dataPointer, size) -> {
                final var chunkBuffer = MemoryUtil.memByteBuffer(dataPointer, size);
                final var chunk = new byte[size];
                chunkBuffer.get(chunk);
                try {
                    out.write(chunk);
                } catch (final IOException e) {
                    throw new RuntimeException("Failed to write image: " + file.getAbsoluteFile());
                }
            })) {
                if (!STBImageWrite.stbi_write_png_to_func(
                        callback,
                        0,
                        image.getWidth(),
                        image.getHeight(),
                        4,
                        buffer,
                        image.getWidth() * 4))
                    throw new RuntimeException("Failed to write image: " + file.getAbsoluteFile());

            } finally {
                MemoryUtil.memFree(buffer);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write image: " + file.getAbsoluteFile(), e);
        }
    }
}
