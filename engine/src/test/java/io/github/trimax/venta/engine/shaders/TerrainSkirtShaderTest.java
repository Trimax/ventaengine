package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.terrain.TerrainChunkLayout;

/**
 * The skirts of the chunks of a terrain (common/heightmap.glsl) on the GPU: the vertices flagged by the layout hang down.
 */
final class TerrainSkirtShaderTest {
    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static float skirt(final float flag, final float depth) {
        return harness.run(List.of("common/heightmap.glsl"), "uniform float flag;",
                "outputColor = vec4(getSkirt(flag));", Map.of("flag", flag, "skirtDepth", depth))[0];
    }

    @Test
    void testVertexOfTheGridDoesNotHang() {
        assertEquals(0.f, skirt(0.f, 5.f));
    }

    @Test
    void testVertexOfASkirtHangsByTheDepth() {
        assertEquals(5.f, skirt(TerrainChunkLayout.SKIRT_FLAG, 5.f), 1e-6f);
        assertEquals(12.5f, skirt(TerrainChunkLayout.SKIRT_FLAG, 12.5f), 1e-6f);
    }

    @Test
    void testOnlyTheFlagMakesASkirt() {
        assertEquals(0.f, skirt(0.2f, 5.f), "Above the grid is not a skirt");
        assertEquals(0.f, skirt(-0.2f, 5.f), "Neither is a little below");
        assertEquals(5.f, skirt(-0.6f, 5.f), 1e-6f, "The flag is read with a margin");
    }
}
