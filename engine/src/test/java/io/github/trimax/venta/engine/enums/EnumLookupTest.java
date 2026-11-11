package io.github.trimax.venta.engine.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class EnumLookupTest {
    @Test
    void testTextureFormatByChannels() {
        assertEquals(TextureFormat.Grayscale, TextureFormat.of(1), "1 channel");
        assertEquals(TextureFormat.RGB, TextureFormat.of(3), "3 channels");
        assertEquals(TextureFormat.RGBA, TextureFormat.of(4), "4 channels");
        assertNull(TextureFormat.of(2), "2 channels are not supported");
        assertNull(TextureFormat.of(0), "0 channels are not supported");
    }

    @Test
    void testGroupTypeByName() {
        for (final var type : GroupType.values()) {
            assertEquals(type, GroupType.of(type.name()), type + " by its name");
            assertEquals(type, GroupType.of(type.name().toUpperCase()), type + " ignoring case");
        }

        assertNull(GroupType.of("unknown"), "Unknown group");
        assertNull(GroupType.of(null), "Null name");
    }
}
