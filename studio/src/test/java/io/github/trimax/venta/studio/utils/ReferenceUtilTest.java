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
    void testTexturesOfAMaterialAreFilesOfTheTextures() {
        for (final var type : io.github.trimax.venta.engine.enums.TextureType.values())
            assertEquals("textures", ReferenceUtil.placeOf(type.getFieldName()).folder(), type.getFieldName());
    }

    @Test
    void testOtherFieldsNameNoFile() {
        assertNull(ReferenceUtil.placeOf("intensity"));
        assertNull(ReferenceUtil.placeOf("name"));
    }
}
