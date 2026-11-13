package io.github.trimax.venta.engine.enums;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.GL_RGB8;
import static org.lwjgl.opengl.GL11C.GL_RGBA16;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL21C.GL_SRGB8;
import static org.lwjgl.opengl.GL21C.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30C.GL_R16;
import static org.lwjgl.opengl.GL30C.GL_R8;

import org.junit.jupiter.api.Test;

final class TextureFormatTest {
    @Test
    void testColorsAreStoredInSRGB() {
        assertEquals(GL_SRGB8, TextureFormat.RGB.getInternal(false, TextureColorSpace.Color), "RGB color");
        assertEquals(GL_SRGB8_ALPHA8, TextureFormat.RGBA.getInternal(false, TextureColorSpace.Color), "RGBA color, the alpha stays linear");
    }

    @Test
    void testDataIsLinear() {
        assertEquals(GL_RGB8, TextureFormat.RGB.getInternal(false, TextureColorSpace.Data), "RGB data (normal map)");
        assertEquals(GL_RGBA8, TextureFormat.RGBA.getInternal(false, TextureColorSpace.Data), "RGBA data");
        assertEquals(GL_R8, TextureFormat.Grayscale.getInternal(false, TextureColorSpace.Data), "Grayscale data (roughness)");
    }

    @Test
    void testGrayscaleColorsStayLinear() {
        assertEquals(GL_R8, TextureFormat.Grayscale.getInternal(false, TextureColorSpace.Color), "There is no single channel sRGB format");
    }

    @Test
    void testSixteenBitImagesKeepPrecision() {
        for (final var colorSpace : TextureColorSpace.values()) {
            assertEquals(GL_R16, TextureFormat.Grayscale.getInternal(true, colorSpace), "16-bit heightmap, " + colorSpace);
            assertEquals(GL_RGBA16, TextureFormat.RGBA.getInternal(true, colorSpace), "16-bit RGBA, " + colorSpace);
        }
    }

    @Test
    void testOnlyDiffuseMapsAreColors() {
        for (final var type : TextureType.values())
            assertEquals(type == TextureType.Diffuse ? TextureColorSpace.Color : TextureColorSpace.Data, type.getColorSpace(), type.name());
    }
}
