package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.enums.Channel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@UtilityClass
public final class ImageUtil {
    public void mix(@NonNull final ImageView sourceRed,
                    @NonNull final ImageView sourceGreen,
                    @NonNull final ImageView sourceBlue,
                    @NonNull final ImageView sourceAlpha,
                    @NonNull final ImageView destination) {
        final var width = (int) sourceRed.getImage().getWidth();
        final var height = (int) sourceRed.getImage().getHeight();

        final var mixedImage = new WritableImage(width, height);
        final var writer = mixedImage.getPixelWriter();

        final var readerRed   = sourceRed.getImage().getPixelReader();
        final var readerGreen = sourceGreen.getImage().getPixelReader();
        final var readerBlue  = sourceBlue.getImage().getPixelReader();
        final var ReaderAlpha = sourceAlpha.getImage().getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final var r = readerRed.getColor(x, y);
                final var g = readerGreen.getColor(x, y);
                final var b = readerBlue.getColor(x, y);
                final var a = ReaderAlpha.getColor(x, y);

                writer.setColor(x, y, new Color(r.getRed(), g.getGreen(), b.getBlue(), a.getOpacity()));
            }
        }

        destination.setImage(mixedImage);
    }

    public void copyChannel(@NonNull final ImageView source,
                            @NonNull final ImageView destination,
                            @NonNull final Channel channel,
                            final int value) {
        final var sourceImage = source.getImage();
        if (sourceImage == null)
            return;

        final var width = (int) sourceImage.getWidth();
        final var height = (int) sourceImage.getHeight();

        final var destinationImage = new WritableImage(width, height);

        final var reader = sourceImage.getPixelReader();
        final var writer = destinationImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final var color = reader.getColor(x, y);

                final var v = switch (channel) {
                    case Red   -> color.getRed();
                    case Green -> color.getGreen();
                    case Blue  -> color.getBlue();
                    case Alpha -> color.getOpacity();
                    case Value -> value / 255.0;
                };

                writer.setColor(x, y, new Color(v, v, v, 1.0));
            }
        }

        destination.setImage(destinationImage);
    }

    public void write(@NonNull final File file, @NonNull final Image image) {
        final var width = (int) image.getWidth();
        final var height = (int) image.getHeight();

        try (var out = new FileOutputStream(file)) {
            final var buffer = MemoryUtil.memAlloc(width * height * 4);
            final var reader = image.getPixelReader();
            if (reader == null)
                throw new IllegalArgumentException("Image has no PixelReader!");

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final var color = reader.getColor(x, y);

                    buffer.put((byte) (255 * color.getRed()));
                    buffer.put((byte) (255 * color.getGreen()));
                    buffer.put((byte) (255 * color.getBlue()));
                    buffer.put((byte) (255 * color.getOpacity()));
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
                        width,
                        height,
                        4,
                        buffer,
                        width * 4))
                    throw new RuntimeException("Failed to write image: " + file.getAbsoluteFile());

            } finally {
                MemoryUtil.memFree(buffer);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write image: " + file.getAbsoluteFile(), e);
        }
    }
}
