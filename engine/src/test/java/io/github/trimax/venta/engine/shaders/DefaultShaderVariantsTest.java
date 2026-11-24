package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * The shader of the objects has two variants made of one file. Only the cutout variant may contain a discard: a fragment
 * shader that can discard loses the early depth test of the GPU, which halved the frame rate of a scene with overdraw.
 */
final class DefaultShaderVariantsTest {
    private static final Pattern DISCARD = Pattern.compile("^\\s*discard\\s*;", Pattern.MULTILINE);

    /** Whether a discard statement is compiled in: the lines that are cut away by the conditions are not counted */
    private static boolean discards(final String code) {
        final var defines = code.contains("#define ALPHA_CUTOUT");

        var active = true;
        var depth = 0;
        var skippedFrom = -1;
        for (final var line : code.split("\n")) {
            final var trimmed = line.trim();
            if (trimmed.startsWith("#ifdef ALPHA_CUTOUT")) {
                depth++;
                if (!defines && active) {
                    active = false;
                    skippedFrom = depth;
                }
            } else if (trimmed.startsWith("#if")) {
                depth++;
            } else if (trimmed.startsWith("#endif")) {
                if (!active && depth == skippedFrom)
                    active = true;

                depth--;
            } else if (active && DISCARD.matcher(line).find())
                return true;
        }

        return false;
    }

    @Test
    void testPlainShaderCanNotDiscard() {
        final var code = ShaderResources.expand("default/fragment.glsl").code();

        assertFalse(code.contains("#define ALPHA_CUTOUT"), "The plain variant doesn't define the cutout");
        assertFalse(discards(code), "A discard in the shader of ordinary objects costs the early depth test");
    }

    @Test
    void testCutoutShaderDiscards() {
        final var code = ShaderResources.expand("default/fragment-cutout.glsl").code();

        assertTrue(code.contains("#define ALPHA_CUTOUT"), "The cutout variant defines the cutout");
        assertTrue(discards(code), "The holes are cut with a discard");
        assertTrue(code.contains("useAlphaCutout"), "A cutout object can still have parts that are not cutout");
    }

    @Test
    void testVariantsAreDefinedBeforeTheSharedCode() {
        final var code = ShaderResources.expand("default/fragment-cutout.glsl").code();

        assertTrue(code.indexOf("#define ALPHA_CUTOUT") < code.indexOf("#ifdef ALPHA_CUTOUT"), "The condition must see the definition");
        assertTrue(code.startsWith("#version 330 core"), "The version comes first");
    }

    @Test
    void testObjectVertexShaderHasTwoVariantsMadeOfOneFile() {
        final var single = ShaderResources.expand("default/vertex.glsl").code();
        final var instanced = ShaderResources.expand("default/vertex-instanced.glsl").code();

        assertFalse(single.contains("#define INSTANCED"), "A single object takes its matrices from the uniforms");
        assertTrue(instanced.contains("#define INSTANCED"), "Copies of a mesh take their matrices from the attributes");
        assertTrue(instanced.indexOf("#define INSTANCED") < instanced.indexOf("#ifdef INSTANCED"), "The condition must see the definition");
        assertTrue(instanced.contains("layout(location = 6) in mat4 instanceModel"));
        assertTrue(instanced.contains("layout(location = 10) in mat3 instanceNormal"));
    }

    @Test
    void testObjectVertexShaderSurvivesVerticesWithoutTangents() {
        final var code = ShaderResources.expand("default/vertex.glsl").code();

        // The loader leaves zeros where the texture coordinates of a vertex are degenerate: normalizing them gives NaN
        assertTrue(code.contains("getTangentSpace"), "The tangent space is built in one place");
        assertTrue(code.contains("dot(t, t) < 1e-12"), "A zero tangent must be replaced before it is normalized");
        assertFalse(code.contains("normalize(normalMatrix * tangent)"), "The tangent must not be normalized as it is");
    }

    @Test
    void testShadowVertexShaderHasFourVariantsMadeOfOneFile() {
        final var single = ShaderResources.expand("shadow/vertex.glsl").code();
        final var cutout = ShaderResources.expand("shadow/cutout-vertex.glsl").code();
        final var instanced = ShaderResources.expand("shadow/vertex-instanced.glsl").code();
        final var cutoutInstanced = ShaderResources.expand("shadow/cutout-vertex-instanced.glsl").code();

        assertFalse(single.contains("#define INSTANCED") || single.contains("#define ALPHA_CUTOUT"));
        assertTrue(cutout.contains("#define ALPHA_CUTOUT") && !cutout.contains("#define INSTANCED"));
        assertTrue(instanced.contains("#define INSTANCED") && !instanced.contains("#define ALPHA_CUTOUT"));
        assertTrue(cutoutInstanced.contains("#define INSTANCED") && cutoutInstanced.contains("#define ALPHA_CUTOUT"));

        for (final var code : new String[]{instanced, cutoutInstanced})
            assertTrue(code.contains("layout(location = 6) in mat4 instanceModel"), "The copies take their matrix from the attributes");
    }
}
