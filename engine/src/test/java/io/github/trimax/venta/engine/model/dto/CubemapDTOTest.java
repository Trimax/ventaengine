package io.github.trimax.venta.engine.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.CubemapFace;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class CubemapDTOTest {
    private static CubemapDTO dto(final String json) {
        return ParsingUtil.asObject(json, CubemapDTO.class);
    }

    private static final String FACES = "\"faces\": {\"Right\": \"a.png\", \"Left\": \"b.png\", \"Top\": \"c.png\", \"Bottom\": \"d.png\", \"Front\": \"e.png\", \"Back\": \"f.png\"}";

    @Test
    void testSkyOfFacesIsReadAsItWas() {
        final var cubemap = dto("{\"program\": \"sky.json\", " + FACES + "}");

        assertDoesNotThrow(cubemap::check);
        assertFalse(cubemap.isEquirectangular());
        assertEquals("a.png", cubemap.getTexturePath(CubemapFace.Right));
        assertEquals("f.png", cubemap.getTexturePath(CubemapFace.Back));
    }

    @Test
    void testSkyOfAPanoramaHasTheDefaults() {
        final var cubemap = dto("{\"program\": \"sky.json\", \"equirectangular\": \"sky/beach.hdr\"}");

        assertDoesNotThrow(cubemap::check);
        assertTrue(cubemap.isEquirectangular());
        assertEquals("sky/beach.hdr", cubemap.equirectangular());
        assertEquals(CubemapDTO.DEFAULT_RESOLUTION, cubemap.getResolution());
        assertEquals(CubemapDTO.DEFAULT_CLAMP, cubemap.getClampValue());
        assertNull(cubemap.getTexturePath(CubemapFace.Right), "A panorama has no faces");
    }

    @Test
    void testResolutionAndClampAreRead() {
        final var cubemap = dto("{\"program\": \"sky.json\", \"equirectangular\": \"a.hdr\", \"resolution\": 512, \"clamp\": 64}");

        assertEquals(512, cubemap.getResolution());
        assertEquals(64.f, cubemap.getClampValue());
    }

    @Test
    void testSkyWithBothFacesAndPanoramaIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"sky.json\", \"equirectangular\": \"a.hdr\", " + FACES + "}").check());
    }

    @Test
    void testSkyWithNeitherIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"sky.json\"}").check());
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"sky.json\", \"faces\": {}}").check());
    }

    @Test
    void testBadResolutionIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"s\", \"equirectangular\": \"a.hdr\", \"resolution\": 8}").getResolution());
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"s\", \"equirectangular\": \"a.hdr\", \"resolution\": 100000}").getResolution());
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"s\", \"equirectangular\": \"a.hdr\", \"resolution\": 0}").getResolution());
    }

    @Test
    void testBadClampIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"s\", \"equirectangular\": \"a.hdr\", \"clamp\": 0}").getClampValue());
        assertThrows(IllegalArgumentException.class, () -> dto("{\"program\": \"s\", \"equirectangular\": \"a.hdr\", \"clamp\": -5}").getClampValue());
    }
}
