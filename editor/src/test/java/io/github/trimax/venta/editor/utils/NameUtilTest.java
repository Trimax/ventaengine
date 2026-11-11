package io.github.trimax.venta.editor.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class NameUtilTest {
    @ParameterizedTest
    @ValueSource(strings = {"textures", "grass_1", "sky-box", "A"})
    void testValidNames(final String name) {
        assertTrue(NameUtil.isValidName(name), name + " must be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "with space", "dot.json", "slash/name", "back\\slash"})
    void testInvalidNames(final String name) {
        assertFalse(NameUtil.isValidName(name), "'" + name + "' must be invalid");
    }
}
