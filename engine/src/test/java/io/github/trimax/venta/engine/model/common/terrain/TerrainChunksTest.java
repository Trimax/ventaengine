package io.github.trimax.venta.engine.model.common.terrain;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.utils.TerrainChunkUtil;

final class TerrainChunksTest {
    private static TerrainChunks create(final float value) {
        final var values = new float[16];
        java.util.Arrays.fill(values, value);

        final var layout = TerrainChunkUtil.build(new Vector2f(80.f, 80.f), new Vector2i(64, 64), new Heightmap(4, 4, values));
        return new TerrainChunks(layout, new Geometry(1, null, null, null));
    }

    @Test
    void testChunksStartAtTheFinestLevel() {
        final var chunks = create(0.5f);

        for (int chunk = 0; chunk < 4; chunk++)
            assertEquals(0, chunks.getLevel(chunk));
    }

    @Test
    void testLevelIsKeptWithinTheLevelsOfTheLayout() {
        final var chunks = create(0.5f);

        chunks.setLevel(0, 1);
        chunks.setLevel(1, 99);
        chunks.setLevel(2, -3);

        assertEquals(1, chunks.getLevel(0));
        assertEquals(4, chunks.getLevel(1), "The coarsest level there is");
        assertEquals(0, chunks.getLevel(2));
    }

    @Test
    void testBoxHoldsTheHeightsAndTheSkirtUnderThem() {
        final var chunks = create(0.75f);

        final var box = chunks.getBox(0, 100.f);
        assertEquals(-40.f, box.min().x, 1e-4f, "The first chunk starts at the corner of the grid");
        assertEquals(-40.f, box.min().z, 1e-4f);
        assertEquals(0.f, box.max().x, 1e-4f, "And is half of it wide");
        assertEquals(0.f, box.max().z, 1e-4f);
        assertEquals(25.f, box.max().y, 1e-3f, "The height: (0.75 - 0.5) * 100");
        assertEquals(25.f - 0.f, box.min().y + chunks.getSkirtDepth(100.f), 1e-3f, "The skirt hangs under the surface");
    }

    @Test
    void testBoxesFollowTheRangeOfTheHeights() {
        final var chunks = create(1.f);

        assertEquals(50.f, chunks.getBox(3, 100.f).max().y, 1e-3f);
        assertEquals(100.f, chunks.getBox(3, 200.f).max().y, 1e-3f, "The boxes are made again for another range");
    }

    @Test
    void testBoxOfTheLastChunkEndsAtTheGridEnd() {
        final var chunks = create(0.5f);

        final var box = chunks.getBox(3, 100.f);
        assertEquals(40.f, box.max().x, 1e-4f);
        assertEquals(40.f, box.max().z, 1e-4f);
        assertEquals(0.f, box.min().x, 1e-4f);
    }

    @Test
    void testSkirtDepthIsAShareOfTheHeightsAndNotLessThanAShareOfAChunk() {
        final var chunks = create(0.5f);

        assertEquals(5.f, chunks.getSkirtDepth(100.f), 1e-4f, "5% of the range");
        assertEquals(1.2f, chunks.getSkirtDepth(0.f), 1e-4f, "A flat terrain still has skirts: 3% of a chunk of 40");
        assertEquals(chunks.getSkirtDepth(100.f), chunks.getSkirtDepth(-100.f), "A negative range is the same depth");
    }
}
