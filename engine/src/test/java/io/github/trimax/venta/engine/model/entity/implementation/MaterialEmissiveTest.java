package io.github.trimax.venta.engine.model.entity.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

/**
 * The light that a material gives out by itself: how it is read from the JSON of a material.
 */
final class MaterialEmissiveTest {
    private static MaterialEntityImplementation material(final String json) {
        return new MaterialEntityImplementation(ParsingUtil.asObject(json, MaterialDTO.class));
    }

    @Test
    void testMaterialGivesOutNothingUnlessAsked() {
        final var emissive = material("{\"metalness\": 0.5}").getEmissive();

        assertEquals(0.f, emissive.x());
        assertEquals(0.f, emissive.y());
        assertEquals(0.f, emissive.z());
    }

    @Test
    void testColorIsTheLightOfTheMaterial() {
        final var emissive = material("{\"emissive\": {\"r\": 1.0, \"g\": 0.75, \"b\": 0.3, \"a\": 1.0}}").getEmissive();

        assertEquals(1.f, emissive.x(), 1e-6f);
        assertEquals(0.75f, emissive.y(), 1e-6f);
        assertEquals(0.3f, emissive.z(), 1e-6f);
    }

    @Test
    void testIntensityMultipliesTheColor() {
        final var emissive = material("{\"emissive\": {\"r\": 1.0, \"g\": 0.5, \"b\": 0.25, \"a\": 1.0}, \"emissiveIntensity\": 4.0}").getEmissive();

        assertEquals(4.f, emissive.x(), 1e-6f, "The light is HDR: it is more than 1");
        assertEquals(2.f, emissive.y(), 1e-6f);
        assertEquals(1.f, emissive.z(), 1e-6f);
    }

    @Test
    void testIntensityOfZeroSwitchesTheLightOffWithoutTakingTheColorAway() {
        final var emissive = material("{\"emissive\": {\"r\": 1.0, \"g\": 1.0, \"b\": 1.0, \"a\": 1.0}, \"emissiveIntensity\": 0.0}").getEmissive();

        assertEquals(0.f, emissive.length());
    }

    @Test
    void testIntensityAloneIsNoLightWithoutAColor() {
        assertEquals(0.f, material("{\"emissiveIntensity\": 5.0}").getEmissive().length(), "It multiplies the color, and there is none");
    }

    @Test
    void testBadLightIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> material("{\"emissive\": {\"r\": 1.0, \"g\": 1.0, \"b\": 1.0, \"a\": 1.0}, \"emissiveIntensity\": -1.0}"));
        assertThrows(IllegalArgumentException.class, () -> material("{\"emissive\": {\"r\": -0.5, \"g\": 1.0, \"b\": 1.0, \"a\": 1.0}}"));
    }

    @Test
    void testEmissiveMapIsATextureType() {
        assertEquals("emissive", TextureType.Emissive.getFieldName());
        assertNotEquals(TextureType.Emissive.getUnit().getId(), TextureType.Diffuse.getUnit().getId());
        assertNotEquals(TextureType.Emissive.getUnit().getId(), TextureType.Normal.getUnit().getId());
        assertNotEquals(TextureType.Emissive.getUnit().getId(), TextureType.AmbientOcclusion.getUnit().getId());
        assertNotEquals(TextureType.Emissive.getUnit().getId(), TextureType.Metalness.getUnit().getId());
        assertNotEquals(TextureType.Emissive.getUnit().getId(), TextureType.Roughness.getUnit().getId());
    }

    @Test
    void testTextureCanBeNamedInTheJsonOfAMaterial() {
        final var dto = ParsingUtil.asObject("{\"textures\": {\"emissive\": \"chest/specular.png\", \"diffuse\": \"chest/diffuse.png\"}}", MaterialDTO.class);

        assertEquals("chest/specular.png", dto.textures().get(TextureType.Emissive));
    }
}
