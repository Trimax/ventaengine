package io.github.trimax.venta.engine.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;

final class MtlMaterialParserTest {
    @Test
    void testMaterials() throws IOException {
        final var materials = parse("""
                # Exported
                newmtl Bark
                Kd 0.4 0.25 0.1
                Ns 0
                d 1.0

                newmtl Leaves Dark
                Kd 0.1 0.5 0.2
                Ns 1000
                Tr 0.25
                map_Kd -bm 1 leaves\\diffuse.png
                map_Bump -bm 0.5 leaves/normal.png
                """);

        assertEquals(List.of("Bark", "Leaves Dark"), List.copyOf(materials.keySet()), "Materials must keep their order and full names");

        final var bark = materials.get("Bark");
        assertEquals(new ColorDTO(0.4f, 0.25f, 0.1f, 1.f), bark.color(), "Kd is the color");
        assertEquals(1.f, bark.roughness(), 1e-6f, "No shininess is a rough surface");
        assertEquals(1.f, bark.opacity(), 1e-6f, "d is the opacity");
        assertNull(bark.textures(), "A material without maps has no textures");

        final var leaves = materials.get("Leaves Dark");
        assertEquals(0.f, leaves.roughness(), 1e-6f, "The highest shininess is a smooth surface");
        assertEquals(0.75f, leaves.opacity(), 1e-6f, "Tr is the transparency");
        assertEquals(Map.of(TextureType.Diffuse, "leaves/diffuse.png", TextureType.Normal, "leaves/normal.png"), leaves.textures(),
                "Map options must be skipped and backslashes replaced");
    }

    @Test
    void testDefaults() throws IOException {
        final MaterialDTO material = parse("newmtl Plain\n").get("Plain");

        assertEquals(new ColorDTO(1.f, 1.f, 1.f, 1.f), material.color(), "The default color is white");
        assertEquals(1.f, material.opacity(), 1e-6f, "The default material is opaque");
        assertEquals(0.f, material.metalness(), 1e-6f, "The default material isn't metallic");
    }

    @Test
    void testStatementsBeforeFirstMaterialAreIgnored() throws IOException {
        assertTrue(parse("Kd 1 0 0\n").isEmpty(), "Statements without a material must be ignored");
    }

    private static Map<String, MaterialDTO> parse(final String text) throws IOException {
        return MtlMaterialParser.parse(new BufferedReader(new StringReader(text)));
    }
}
