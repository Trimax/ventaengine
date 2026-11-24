package io.github.trimax.venta.engine.model.entity.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.AlphaMode;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

/**
 * The alpha settings of a material: how they are read from the JSON of a material and what they default to.
 */
final class MaterialAlphaTest {
    private static MaterialEntityImplementation material(final String json) {
        return new MaterialEntityImplementation(ParsingUtil.asObject(json, MaterialDTO.class));
    }

    @Test
    void testMaterialWithoutAlphaSettingsMixesColorsAndIsDoubleSided() {
        final var material = material("{\"metalness\": 0.5}");

        assertEquals(AlphaMode.Blend, material.getAlphaMode(), "The alpha mixes colors as before");
        assertFalse(material.isCutout());
        assertTrue(material.isDoubleSided(), "Both sides are drawn as before");
        assertEquals(MaterialEntityImplementation.DEFAULT_ALPHA_CUTOFF, material.getAlphaCutoff());
    }

    @Test
    void testAlphaSettingsAreRead() {
        final var material = material("{\"alphaMode\": \"cutout\", \"alphaCutoff\": 0.3, \"doubleSided\": false}");

        assertEquals(AlphaMode.Cutout, material.getAlphaMode());
        assertTrue(material.isCutout());
        assertEquals(0.3f, material.getAlphaCutoff());
        assertFalse(material.isDoubleSided());
    }

    @Test
    void testModeIsReadInAnyCase() {
        assertEquals(AlphaMode.Cutout, material("{\"alphaMode\": \"Cutout\"}").getAlphaMode());
        assertEquals(AlphaMode.Cutout, material("{\"alphaMode\": \"CUTOUT\"}").getAlphaMode());
        assertEquals(AlphaMode.Blend, material("{\"alphaMode\": \"blend\"}").getAlphaMode());
        assertEquals(AlphaMode.Blend, material("{\"alphaMode\": \"BLEND\"}").getAlphaMode());
    }

    @Test
    void testCutoffIsFromZeroToOne() {
        assertEquals(0.f, material("{\"alphaCutoff\": 0}").getAlphaCutoff(), "Nothing is cut");
        assertEquals(1.f, material("{\"alphaCutoff\": 1}").getAlphaCutoff(), "Everything below the full alpha is cut");

        for (final var cutoff : new String[]{"-0.1", "1.1", "5", "-1"})
            assertThrows(IllegalArgumentException.class, () -> material("{\"alphaCutoff\": " + cutoff + "}"), "The cutoff " + cutoff);
    }

    @Test
    void testOtherSettingsAreKept() {
        final var material = material("{\"metalness\": 0.7, \"roughness\": 0.2, \"alphaMode\": \"cutout\"}");

        assertEquals(0.7f, material.getMetalness());
        assertEquals(0.2f, material.getRoughness());
    }

    @Test
    void testMaterialDoesNotFollowTheWindByDefault() {
        final var material = material("{\"metalness\": 0.5}");

        assertEquals(0.f, material.getWindInfluence());
        assertFalse(material.followsWind());
    }

    @Test
    void testWindInfluenceIsRead() {
        final var material = material("{\"windInfluence\": 0.6}");

        assertEquals(0.6f, material.getWindInfluence());
        assertTrue(material.followsWind());
    }

    @Test
    void testWindInfluenceMustBeFromZeroToOne() {
        assertThrows(IllegalArgumentException.class, () -> material("{\"windInfluence\": 1.5}"));
        assertThrows(IllegalArgumentException.class, () -> material("{\"windInfluence\": -0.1}"));
    }
}
