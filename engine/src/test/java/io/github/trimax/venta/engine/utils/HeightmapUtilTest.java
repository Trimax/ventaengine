package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;

final class HeightmapUtilTest {
    private static final float EPSILON = 1e-5f;

    @Test
    void testRedChannelIsDecodedRowByRow() throws IOException {
        final var image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x000000);
        image.setRGB(1, 0, 0xFF00FF);
        image.setRGB(0, 1, 0x33FFFF);
        image.setRGB(1, 1, 0xCC0000);

        final var data = toBuffer(image);
        final var heightmap = HeightmapUtil.decode("test.png", data);

        assertEquals(2, heightmap.getWidth());
        assertEquals(2, heightmap.getHeight());
        assertEquals(0.f, heightmap.sample(0.25f, 0.25f), EPSILON);
        assertEquals(1.f, heightmap.sample(0.75f, 0.25f), EPSILON);
        assertEquals(0.2f, heightmap.sample(0.25f, 0.75f), EPSILON);
        assertEquals(0.8f, heightmap.sample(0.75f, 0.75f), EPSILON);
        assertEquals(0, data.position(), "Buffer position must be kept");
    }

    @Test
    void testGrayscaleImageIsDecoded() throws IOException {
        final var image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setSample(0, 0, 0, 51);

        assertEquals(0.2f, HeightmapUtil.decode("gray.png", toBuffer(image)).sample(0.5f, 0.5f), EPSILON);
    }

    @Test
    void testBrokenImageIsRejected() {
        final var data = ByteBuffer.allocateDirect(4).put(new byte[]{1, 2, 3, 4}).flip();

        assertThrows(UnknownTextureFormatException.class, () -> HeightmapUtil.decode("broken.png", data));
    }

    private static ByteBuffer toBuffer(final BufferedImage image) throws IOException {
        final var stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);

        final var bytes = stream.toByteArray();
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }
}
