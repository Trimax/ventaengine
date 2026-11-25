package io.github.trimax.venta.engine.model.entity.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class MaterialReflectionsTest {
    private static MaterialEntityImplementation material(final String json) throws Exception {
        final var constructor = MaterialEntityImplementation.class.getDeclaredConstructor(MaterialDTO.class);
        constructor.setAccessible(true);

        return constructor.newInstance(ParsingUtil.asObject(json, MaterialDTO.class));
    }

    @Test
    void testMaterialReflectsTheSkyboxByDefault() throws Exception {
        final var material = material("{}");

        assertFalse(material.hasReflections());
        assertNull(material.getReflections());
    }

    @Test
    void testMaterialAsksForDynamicReflections() throws Exception {
        final var material = material("{\"dynamicReflections\": {\"resolution\": 64, \"refresh\": 1.5, \"range\": 20}}");

        assertTrue(material.hasReflections());
        assertEquals(64, material.getReflections().getResolution());
        assertEquals(1.5f, material.getReflections().getRefresh());
        assertEquals(20.f, material.getReflections().getRange());
    }

    @Test
    void testEmptySettingsAreTheDefaults() throws Exception {
        final var material = material("{\"dynamicReflections\": {}}");

        assertTrue(material.hasReflections());
        assertEquals(128, material.getReflections().getResolution());
    }

    @Test
    void testBadSettingsAreRefused() {
        final var exception = assertThrows(Exception.class, () -> material("{\"dynamicReflections\": {\"resolution\": 4}}"));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }
}
