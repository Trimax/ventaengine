package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.terrain.Heightmap;
import io.github.trimax.venta.engine.model.common.terrain.TerrainChunks;
import io.github.trimax.venta.engine.utils.TerrainChunkUtil;

final class TerrainChunkHelperTest {
    private static final float FACTOR = 20.f;

    private final TerrainChunkHelper helper = create(null);
    private final Matrix4f identity = new Matrix4f();

    /** The constructor of a component is private: the container makes it, the test goes the same way */
    private static TerrainChunkHelper create(final GeometryHelper geometryHelper) {
        try {
            final var constructor = TerrainChunkHelper.class.getDeclaredConstructor(GeometryHelper.class);
            constructor.setAccessible(true);
            return constructor.newInstance(geometryHelper);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @AfterEach
    void release() {
        helper.release();
    }

    /** A terrain of 8 by 8 chunks of 32 cells in 8 by 8 units each: 256 cells and 64 units wide */
    private static TerrainChunks createChunks() {
        final var values = new float[16];
        java.util.Arrays.fill(values, 0.5f);

        final var layout = TerrainChunkUtil.build(new Vector2f(512.f, 512.f), new Vector2i(256, 256), new Heightmap(4, 4, values));
        return new TerrainChunks(layout, new Geometry(1, null, null, null));
    }

    @Test
    void testLevelIsFinestUpToTheBaseDistanceAndCoarserAfterEveryDoubling() {
        assertEquals(0, TerrainChunkHelper.getLevel(0.f, 100.f, 5));
        assertEquals(0, TerrainChunkHelper.getLevel(99.f, 100.f, 5));
        assertEquals(1, TerrainChunkHelper.getLevel(100.f, 100.f, 5));
        assertEquals(1, TerrainChunkHelper.getLevel(199.f, 100.f, 5));
        assertEquals(2, TerrainChunkHelper.getLevel(200.f, 100.f, 5));
        assertEquals(3, TerrainChunkHelper.getLevel(400.f, 100.f, 5));
        assertEquals(4, TerrainChunkHelper.getLevel(800.f, 100.f, 5));
    }

    @Test
    void testLevelNeverGoesPastTheCoarsest() {
        assertEquals(4, TerrainChunkHelper.getLevel(1.0e7f, 100.f, 5));
        assertEquals(0, TerrainChunkHelper.getLevel(1.0e7f, 100.f, 1), "One level is the only one");
    }

    @Test
    void testLevelNeverGetsFinerWithTheDistance() {
        var previous = 0;
        for (var distance = 0.f; distance < 5000.f; distance += 7.f) {
            final var level = TerrainChunkHelper.getLevel(distance, 60.f, 5);
            assertTrue(level >= previous, "Farther is not finer: " + distance);
            previous = level;
        }
    }

    @Test
    void testBadBaseDistanceGivesTheFinestLevel() {
        assertEquals(0, TerrainChunkHelper.getLevel(500.f, 0.f, 5));
        assertEquals(0, TerrainChunkHelper.getLevel(500.f, -3.f, 5));
        assertEquals(0, TerrainChunkHelper.getLevel(Float.NaN, 100.f, 5));
    }

    @Test
    void testNearestChunksAreFinestAndFarOnesAreCoarser() {
        final var chunks = createChunks();

        // The camera is over the first chunk (the chunks are 64 units wide, the first level changes at 1.5 of them: 96)
        helper.update(chunks, identity, new Vector3f(-224.f, 5.f, -224.f), FACTOR, 0.f);

        assertEquals(0, chunks.getLevel(0), "Under the camera");
        assertEquals(0, chunks.getLevel(1), "The next one is near");
        assertTrue(chunks.getLevel(63) > chunks.getLevel(0), "The opposite corner is far");
        assertEquals(3, chunks.getLevel(63), "The farthest corner is 588 units from the camera, 6 times the first distance: the level 3");
    }

    @Test
    void testLevelOfAChunkIsNotFinerWithTheDistanceFromTheCamera() {
        final var chunks = createChunks();
        helper.update(chunks, identity, new Vector3f(-224.f, 5.f, -224.f), FACTOR, 0.f);

        for (int row = 0; row < 8; row++)
            for (int column = 1; column < 8; column++)
                assertTrue(chunks.getLevel(row * 8 + column) >= chunks.getLevel(row * 8 + column - 1) || row > 0,
                        "Along the first row, away from the camera");
    }

    @Test
    void testDistanceToAChunkIsTheDistanceToItsNearestPoint() {
        final var chunks = createChunks();

        // 100 units over the middle of the terrain: the nearest chunks are 100 away, and the base distance is 150
        helper.update(chunks, identity, new Vector3f(0.f, 100.f, 0.f), 0.f, 150.f);

        assertEquals(0, chunks.getLevel(27), "A chunk next to the middle is 100 away");
        assertEquals(0, chunks.getLevel(36));
    }

    @Test
    void testScaleOfTheTerrainScalesTheDistances() {
        final var small = createChunks();
        final var big = createChunks();

        helper.update(small, identity, new Vector3f(-224.f, 5.f, -224.f), FACTOR, 0.f);
        helper.update(big, new Matrix4f().scale(4.f, 1.f, 4.f), new Vector3f(-224.f * 4.f, 5.f, -224.f * 4.f), FACTOR, 0.f);

        for (int chunk = 0; chunk < 64; chunk++)
            assertEquals(small.getLevel(chunk), big.getLevel(chunk), "The same view of a terrain that is 4 times bigger: chunk " + chunk);
    }

    @Test
    void testTerrainThatSaysItsLevelDistanceIsObeyed() {
        final var chunks = createChunks();

        helper.update(chunks, identity, new Vector3f(-224.f, 5.f, -224.f), FACTOR, 2000.f);

        for (int chunk = 0; chunk < 64; chunk++)
            assertEquals(0, chunks.getLevel(chunk), "All of it is nearer than the first change");
    }

    @Test
    void testChunksOutOfTheViewAreNotDrawn() {
        final var chunks = createChunks();
        final var culler = new FrustumHelper();

        // The camera looks from the middle of the left edge along x: half of the terrain is behind it
        final var projection = new Matrix4f().perspective((float) Math.toRadians(60), 1.f, 0.1f, 1000.f);
        final var view = new Matrix4f().lookAt(-260.f, 30.f, 0.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        culler.set(projection.mul(view), FrustumHelper.ALL_PLANES);

        final var visible = helper.select(chunks, identity, FACTOR, culler);

        assertTrue(visible > 0, "Something is seen");
        assertTrue(visible < 64, "Not all of it: " + visible);
    }

    @Test
    void testChunksAllAreDrawnWhenTheViewSeesThemAll() {
        final var chunks = createChunks();
        final var culler = new FrustumHelper();

        final var projection = new Matrix4f().perspective((float) Math.toRadians(90), 1.f, 0.1f, 2000.f);
        final var view = new Matrix4f().lookAt(0.f, 500.f, 1.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        culler.set(projection.mul(view), FrustumHelper.ALL_PLANES);

        assertEquals(64, helper.select(chunks, identity, FACTOR, culler));
    }

    @Test
    void testSelectedChunksHaveTheRangesOfTheirLevels() {
        final var chunks = createChunks();
        final var layout = chunks.getLayout();
        final var culler = new FrustumHelper();

        final var projection = new Matrix4f().perspective((float) Math.toRadians(90), 1.f, 0.1f, 2000.f);
        final var view = new Matrix4f().lookAt(0.f, 500.f, 1.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        culler.set(projection.mul(view), FrustumHelper.ALL_PLANES);

        chunks.setLevel(0, 0);
        chunks.setLevel(1, 2);
        chunks.setLevel(2, 4);

        final var parts = helper.select(chunks, identity, FACTOR, culler);
        assertEquals(64, parts);

        for (int chunk = 0; chunk < 3; chunk++) {
            assertEquals(layout.getCount(chunk, chunks.getLevel(chunk)), helper.getSelectedCount(chunk), "The count of chunk " + chunk);
            assertEquals((long) layout.getOffset(chunk, chunks.getLevel(chunk)) * Integer.BYTES, helper.getSelectedOffset(chunk), "The offset in bytes of chunk " + chunk);
        }

        assertTrue(helper.getSelectedCount(2) < helper.getSelectedCount(0), "A coarser level has fewer indices");
    }

    @Test
    void testTrianglesOfTheFrameAreCountedAndStartedAgain() {
        final var chunks = createChunks();
        final var culler = new FrustumHelper();

        final var projection = new Matrix4f().perspective((float) Math.toRadians(90), 1.f, 0.1f, 2000.f);
        final var view = new Matrix4f().lookAt(0.f, 500.f, 1.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
        culler.set(projection.mul(view), FrustumHelper.ALL_PLANES);

        assertEquals(0, helper.getTriangles());

        helper.select(chunks, identity, FACTOR, culler);
        assertEquals(0, helper.getTriangles(), "A frame that has not ended is not in the count");

        // 64 chunks of the finest level: 1024 quads and 128 skirt quads of two triangles each
        helper.startFrame();
        assertEquals(64 * (1024 + 128) * 2, helper.getTriangles());

        helper.startFrame();
        assertEquals(0, helper.getTriangles(), "The next frame starts from nothing");
    }
}
