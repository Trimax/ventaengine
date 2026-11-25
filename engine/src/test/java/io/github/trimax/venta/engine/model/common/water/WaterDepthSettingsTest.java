package io.github.trimax.venta.engine.model.common.water;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.water.WaterFoamDTO;
import io.github.trimax.venta.engine.model.dto.water.WaterMaterialDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class WaterDepthSettingsTest {
    private static final String COLORS = "\"colorSurface\": {\"r\": 0.1, \"g\": 0.2, \"b\": 0.3, \"a\": 1}, "
            + "\"colorDepth\": {\"r\": 0, \"g\": 0, \"b\": 0.1, \"a\": 1}, \"colorPeak\": {\"r\": 1, \"g\": 1, \"b\": 1, \"a\": 1}, ";

    private static WaterMaterial material(final String extra) {
        return new WaterMaterial(ParsingUtil.asObject("{" + COLORS + "\"metalness\": 1.0, \"opacity\": 1.0" + extra + "}", WaterMaterialDTO.class));
    }

    private static WaterFoam foam(final String extra) {
        return new WaterFoam(ParsingUtil.asObject("{\"threshold\": 0.9, \"intensity\": 0.2" + extra + "}", WaterFoamDTO.class));
    }

    @Test
    void testMaterialWithoutDepthSettingsHasTheDefaults() {
        final var material = material("");

        assertEquals(WaterMaterial.DEFAULT_DEPTH_DISTANCE, material.getDepthDistance());
        assertEquals(WaterMaterial.DEFAULT_REFRACTION, material.getRefraction());
    }

    @Test
    void testDepthSettingsAreRead() {
        final var material = material(", \"depthDistance\": 20, \"refraction\": 0.1");

        assertEquals(20.f, material.getDepthDistance());
        assertEquals(0.1f, material.getRefraction());
    }

    @Test
    void testBadDepthSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> material(", \"depthDistance\": 0"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"depthDistance\": -3"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"refraction\": -0.1"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"refraction\": 0.9"));
    }

    @Test
    void testReflectionsAreOffUnlessAsked() {
        assertFalse(material("").isReflections());
        assertTrue(material(", \"reflections\": true").isReflections());
        assertFalse(material(", \"reflections\": false").isReflections());
        assertTrue(new WaterMaterial(material(", \"reflections\": true")).isReflections(), "A copy keeps it");
    }

    @Test
    void testRefractionOfZeroSwitchesItOff() {
        assertEquals(0.f, material(", \"refraction\": 0").getRefraction());
    }

    @Test
    void testCopyOfMaterialKeepsTheDepthSettings() {
        final var copy = new WaterMaterial(material(", \"depthDistance\": 12, \"refraction\": 0.2"));

        assertEquals(12.f, copy.getDepthDistance());
        assertEquals(0.2f, copy.getRefraction());
    }

    @Test
    void testFoamWithoutShoreSettingsHasTheDefaults() {
        final var foam = foam("");

        assertEquals(WaterFoam.DEFAULT_SHORE_WIDTH, foam.getShoreWidth());
        assertEquals(WaterFoam.DEFAULT_SHORE_INTENSITY, foam.getShoreIntensity());
    }

    @Test
    void testShoreSettingsAreReadAndCopied() {
        final var foam = new WaterFoam(foam(", \"shoreWidth\": 3, \"shoreIntensity\": 0.5"));

        assertEquals(3.f, foam.getShoreWidth());
        assertEquals(0.5f, foam.getShoreIntensity());
    }

    @Test
    void testBadShoreSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> foam(", \"shoreWidth\": -1"));
        assertThrows(IllegalArgumentException.class, () -> foam(", \"shoreIntensity\": 1.5"));
        assertThrows(IllegalArgumentException.class, () -> foam(", \"shoreIntensity\": -0.1"));
    }

    @Test
    void testShoreWidthOfZeroSwitchesTheShoreFoamOff() {
        assertEquals(0.f, foam(", \"shoreWidth\": 0").getShoreWidth());
    }

    @Test
    void testWavesAreNotFadedAndHaveNoSwashUnlessAsked() {
        final var material = material("");

        assertEquals(0.f, material.getWaveFadeDistance());
        assertEquals(0.f, material.getSwashAmplitude());
        assertEquals(WaterMaterial.DEFAULT_SWASH_PERIOD, material.getSwashPeriod());
    }

    @Test
    void testFadeAndSwashAreReadAndCopied() {
        final var material = new WaterMaterial(material(", \"waveFadeDistance\": 900, \"swashAmplitude\": 0.4, \"swashPeriod\": 10"));

        assertEquals(900.f, material.getWaveFadeDistance());
        assertEquals(0.4f, material.getSwashAmplitude());
        assertEquals(10.f, material.getSwashPeriod());
    }

    @Test
    void testBadFadeAndSwashAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> material(", \"waveFadeDistance\": -1"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"swashAmplitude\": -0.1"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"swashPeriod\": 0"));
        assertThrows(IllegalArgumentException.class, () -> material(", \"swashPeriod\": -4"));
    }

    @Test
    void testBandsOfFoamAreOffUnlessAsked() {
        assertEquals(0.f, foam("").getBands());
        assertEquals(0.6f, new WaterFoam(foam(", \"bands\": 0.6")).getBands());
        assertThrows(IllegalArgumentException.class, () -> foam(", \"bands\": 1.5"));
        assertThrows(IllegalArgumentException.class, () -> foam(", \"bands\": -0.1"));
    }
}
