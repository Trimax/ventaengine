package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class ParsingUtilTest {
    @Test
    void testScalars() {
        assertTrue(ParsingUtil.asBoolean("true"), "true");
        assertFalse(ParsingUtil.asBoolean("no"), "Anything else is false");
        assertEquals(42, ParsingUtil.asInteger("42"), "Integer");
        assertEquals(-1.5f, ParsingUtil.asFloat("-1.5"), "Float");
    }

    @Test
    void testVectors() {
        assertEquals(new Vector2f(1, -2), ParsingUtil.asVector2f("[1 -2]"), "Vector2f");
        assertEquals(new Vector3f(1, 2.5f, 3), ParsingUtil.asVector3f("  [ 1   2.5 3 ]  "), "Vector3f with extra spaces");
    }

    @Test
    void testInvalidVectorsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ParsingUtil.asVector3f("1 2 3"), "Missing brackets");
        assertThrows(IllegalArgumentException.class, () -> ParsingUtil.asVector3f("[1 2]"), "Too few components");
        assertThrows(IllegalArgumentException.class, () -> ParsingUtil.asVector2f("[1 2 3]"), "Too many components");
        assertThrows(NumberFormatException.class, () -> ParsingUtil.asVector2f("[a b]"), "Not numbers");
    }
}
