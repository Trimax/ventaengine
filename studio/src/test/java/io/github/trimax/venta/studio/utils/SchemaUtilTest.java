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

    @Test
    void testElementOfAListIsFoundByItsNumber() {
        final var water = io.github.trimax.venta.engine.model.dto.WaterSurfaceDTO.class;

        assertEquals(List.class, SchemaUtil.typeOf(water, List.of("waves")));
        assertNotNull(SchemaUtil.typeOf(water, List.of("waves", "0")));
        assertEquals(float.class, SchemaUtil.typeOf(water, List.of("waves", "2", "amplitude")));
        assertNull(SchemaUtil.typeOf(water, List.of("waves", "0", "nothing")));
    }

    @Test
    void testFieldOfAGenericRecordIsNotKnown() {
        // The parameter of Variable<Float> is not known from the record: the editor shows a number as a fraction
        assertNull(SchemaUtil.typeOf(io.github.trimax.venta.engine.model.dto.EmitterDTO.class, List.of("particleLifetime", "value")));
    }
}
