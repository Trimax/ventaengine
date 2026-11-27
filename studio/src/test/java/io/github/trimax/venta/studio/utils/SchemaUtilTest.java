package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.AntialiasingType;
import io.github.trimax.venta.engine.enums.ToneMapping;

final class SchemaUtilTest {
    @Test
    void testTypeOfAFieldOfASectionIsTheTypeOfTheRecordComponent() {
        assertEquals(AntialiasingType.class, SchemaUtil.typeOf("Antialiasing", List.of("type")));
        assertEquals(Float.class, SchemaUtil.typeOf("Antialiasing", List.of("subpixel")));
    }

    @Test
    void testTypeOfAFieldInsideAFieldIsFound() {
        assertNotNull(SchemaUtil.typeOf("Sun", List.of("color")));
        assertEquals(float.class, SchemaUtil.typeOf("Sun", List.of("color", "r")));
    }

    @Test
    void testTypeOfAFieldOfAResourceRecord() {
        assertEquals(io.github.trimax.venta.engine.enums.LightType.class, SchemaUtil.typeOf(io.github.trimax.venta.engine.model.dto.LightDTO.class, List.of("type")));
        assertEquals(boolean.class, SchemaUtil.typeOf(io.github.trimax.venta.engine.model.dto.LightDTO.class, List.of("castShadows")));
        assertNull(SchemaUtil.typeOf(io.github.trimax.venta.engine.model.dto.LightDTO.class, List.of("nothing")));
    }

    @Test
    void testFieldsOfTheOutputAreFieldsOfTheScene() {
        assertEquals(ToneMapping.class, SchemaUtil.typeOf("Output", List.of("toneMapping")));
    }

    @Test
    void testUnknownFieldOrSectionHasNoType() {
        assertNull(SchemaUtil.typeOf("Fog", List.of("nothing")));
        assertNull(SchemaUtil.typeOf("Fog", List.of("density", "deeper")));
        assertNull(SchemaUtil.typeOf("Nonsense", List.of()));
    }

    @Test
    void testValuesOfAnEnumAreItsNames() {
        assertTrue(SchemaUtil.constantsOf(ToneMapping.class).contains("ACES"));
        assertNull(SchemaUtil.constantsOf(Float.class));
        assertNull(SchemaUtil.constantsOf(null));
    }

    @Test
    void testWholeNumbersAreToldFromFractions() {
        assertTrue(SchemaUtil.isWhole(int.class));
        assertTrue(SchemaUtil.isWhole(Integer.class));
        assertFalse(SchemaUtil.isWhole(Float.class));
        assertFalse(SchemaUtil.isWhole(null));
    }
}
