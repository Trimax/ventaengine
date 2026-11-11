package io.github.trimax.venta.editor.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.editor.enums.Channel;

final class ImageUtilTest {
    private static final int PIXEL = 0x80_10_20_30; // a=128, r=16, g=32, b=48

    @Test
    void testExtractChannels() {
        final var source = image(PIXEL);

        assertEquals(16, sample(ImageUtil.extract(source, Channel.Red, 0)), "Red channel must be extracted");
        assertEquals(32, sample(ImageUtil.extract(source, Channel.Green, 0)), "Green channel must be extracted");
        assertEquals(48, sample(ImageUtil.extract(source, Channel.Blue, 0)), "Blue channel must be extracted");
        assertEquals(128, sample(ImageUtil.extract(source, Channel.Alpha, 0)), "Alpha channel must be extracted");
        assertEquals(77, sample(ImageUtil.extract(source, Channel.Value, 77)), "Exact value must be used");
    }

    @Test
    void testAlphaOfOpaqueImageIsMaximal() {
        final var source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, 0x102030);

        assertEquals(255, sample(ImageUtil.extract(source, Channel.Alpha, 0)), "Image without alpha must be opaque");
    }

    @Test
    void testMixChannels() {
        final var mixed = ImageUtil.mix(gray(1), gray(2), gray(3), gray(4));

        assertEquals(0x04_01_02_03, mixed.getRGB(0, 0), "Channels must be mixed into ARGB");
    }

    @Test
    void testWriteAndReadBack(@TempDir final Path directory) throws Exception {
        final var file = directory.resolve("image.png").toFile();
        final var source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 3; x++)
                source.setRGB(x, y, 0xFF_00_00_00 | (x * 40) << 16 | (y * 80) << 8 | 7);

        ImageUtil.write(file, source);

        final var loaded = ImageIO.read(file);
        assertEquals(3, loaded.getWidth(), "Width must be kept");
        assertEquals(2, loaded.getHeight(), "Height must be kept");
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 3; x++)
                assertEquals(source.getRGB(x, y), loaded.getRGB(x, y), "Pixel " + x + "," + y + " must be kept");
    }

    private static BufferedImage image(final int argb) {
        final var image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 2; x++)
                image.setRGB(x, y, argb);

        return image;
    }

    private static BufferedImage gray(final int value) {
        final var image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setSample(0, 0, 0, value);
        return image;
    }

    private static int sample(final BufferedImage image) {
        return image.getRaster().getSample(0, 0, 0);
    }
}
