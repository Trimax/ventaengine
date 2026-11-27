package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class ReferenceUtilTest {
    @Test
    void testFieldThatNamesAFileSaysWhereTheFilesAre() {
        assertEquals("materials", ReferenceUtil.placeOf("material").folder());
        assertEquals("meshes", ReferenceUtil.placeOf("mesh").folder());
        assertTrue(ReferenceUtil.placeOf("texture").extensions().contains(".png"));
    }

    @Test
    void testOtherFieldsNameNoFile() {
        assertNull(ReferenceUtil.placeOf("intensity"));
        assertNull(ReferenceUtil.placeOf("name"));
    }
}
