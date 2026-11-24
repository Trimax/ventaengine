package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

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
}
