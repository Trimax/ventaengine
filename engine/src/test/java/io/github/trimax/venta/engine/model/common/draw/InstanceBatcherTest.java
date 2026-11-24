package io.github.trimax.venta.engine.model.common.draw;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.layouts.InstanceVertexLayout;

final class InstanceBatcherTest {
    private static final int FLOATS = InstanceVertexLayout.getFloatsCount();

    private final InstanceBatcher<String, String, String> batcher = new InstanceBatcher<>();

    @Test
    void testCopiesOfOneKindMakeOneBatch() {
        final var shape = "tree";
        for (int i = 0; i < 5; i++)
            batcher.add(shape, "program", "leaves", true, new Matrix4f().translation(i, 0, 0));

        assertEquals(1, batcher.getBatches().size());
        assertEquals(5, batcher.getBatches().getFirst().getCount());
        assertEquals(5 * FLOATS, batcher.getBatches().getFirst().getLength());
    }

    @Test
    void testBatchesAreSplitByMaterialProgramLightingAndShape() {
        final var model = new Matrix4f();
        final var tree = "tree";

        batcher.add(tree, "program", "leaves", true, model);
        batcher.add(tree, "program", "bark", true, model);
        batcher.add(tree, "other program", "leaves", true, model);
        batcher.add(tree, "program", "leaves", false, model);
        batcher.add("rock", "program", "leaves", true, model);
        batcher.add(tree, "program", "leaves", true, model);

        assertEquals(5, batcher.getBatches().size());
        assertEquals(2, batcher.getBatches().getFirst().getCount(), "The last copy joins the first batch");
    }

    @Test
    void testShapesAreComparedByReference() {
        batcher.add(new String("tree"), "program", "leaves", true, new Matrix4f());
        batcher.add(new String("tree"), "program", "leaves", true, new Matrix4f());

        assertEquals(2, batcher.getBatches().size(), "Equal but different meshes are different meshes");
    }

    @Test
    void testBatchesKeepTheOrderOfTheirCreation() {
        batcher.add("b", "program", "material", true, new Matrix4f());
        batcher.add("a", "program", "material", true, new Matrix4f());
        batcher.add("b", "program", "material", true, new Matrix4f());

        assertEquals("b", batcher.getBatches().get(0).getShape());
        assertEquals("a", batcher.getBatches().get(1).getShape());
    }

    @Test
    void testMatricesAreStoredInTheOrderOfTheLayout() {
        batcher.add("tree", "program", "material", true, new Matrix4f().translation(1, 2, 3).scale(2));
        batcher.add("tree", "program", "material", true, new Matrix4f().translation(4, 5, 6));

        final var data = batcher.getBatches().getFirst().getData();
        final var model = InstanceVertexLayout.MatrixModel.getSize();

        // The translation is the last column of the model matrix
        assertEquals(1, data[12]);
        assertEquals(2, data[13]);
        assertEquals(3, data[14]);
        assertEquals(1, data[15]);

        // The normal matrix of a uniform scale 2 is the identity scaled by 1 / 2
        assertEquals(0.5f, data[model], 1e-6);
        assertEquals(0.5f, data[model + 4], 1e-6);
        assertEquals(0.5f, data[model + 8], 1e-6);
        assertEquals(0f, data[model + 1], 1e-6);

        // The second copy follows the first one
        assertEquals(4, data[FLOATS + 12]);
        assertEquals(5, data[FLOATS + 13]);
        assertEquals(6, data[FLOATS + 14]);
        assertEquals(1f, data[FLOATS + model], 1e-6, "The normal matrix of a translation is the identity");
    }

    @Test
    void testDataGrowsWithTheCopies() {
        for (int i = 0; i < 1000; i++)
            batcher.add("tree", "program", "material", true, new Matrix4f().translation(i, 0, 0));

        final var batch = batcher.getBatches().getFirst();
        assertEquals(1000, batch.getCount());
        assertTrue(batch.getData().length >= batch.getLength());
        assertEquals(999, batch.getData()[999 * FLOATS + 12], "The copies survive the growth");
    }

    @Test
    void testClearForgetsTheCopiesAndReusesTheBatches() {
        batcher.add("tree", "program", "material", true, new Matrix4f());
        final var first = batcher.getBatches().getFirst();

        batcher.clear();
        assertTrue(batcher.getBatches().isEmpty());
        assertNull(first.getShape(), "The batch doesn't keep the mesh alive");
        assertNull(first.getMaterial());
        assertEquals(0, first.getCount());

        batcher.add("rock", "program", "material", true, new Matrix4f());
        assertSame(first, batcher.getBatches().getFirst(), "The batch is reused by the next frame");
        assertEquals("rock", first.getShape());
        assertEquals(1, first.getCount());
    }

    @Test
    void testMaterialMayBeMissing() {
        batcher.add("tree", "program", null, true, new Matrix4f());
        batcher.add("tree", "program", null, true, new Matrix4f());

        assertEquals(1, batcher.getBatches().size());
        assertNull(batcher.getBatches().getFirst().getMaterial());
    }

    @Test
    void testShapeAndMatrixAreRequired() {
        assertThrows(NullPointerException.class, () -> batcher.add(null, "program", "material", true, new Matrix4f()));
        assertThrows(NullPointerException.class, () -> batcher.add("tree", "program", "material", true, null));
    }

    @Test
    void testCopiesAreWholeUnlessTheyFade() {
        batcher.add("tree", "program", "material", true, new Matrix4f());

        final var batch = batcher.getBatches().getFirst();
        final var fade = InstanceVertexLayout.MatrixModel.getSize() + InstanceVertexLayout.MatrixNormal.getSize();
        assertEquals(1.f, batch.getData()[fade], "A copy is drawn whole by default");
        assertFalse(batch.isFading());
    }

    @Test
    void testFadingCopyMakesTheBatchFadingAndKeepsItsFade() {
        final var fade = InstanceVertexLayout.MatrixModel.getSize() + InstanceVertexLayout.MatrixNormal.getSize();
        batcher.add("tree", "program", "material", true, new Matrix4f());
        batcher.add("tree", "program", "material", true, new Matrix4f(), 0.25f);
        batcher.add("tree", "program", "material", true, new Matrix4f(), -0.75f);

        final var batch = batcher.getBatches().getFirst();
        assertEquals(3, batch.getCount());
        assertTrue(batch.isFading(), "One copy that fades makes the batch need the shader with the dither");
        assertEquals(1.f, batch.getData()[fade]);
        assertEquals(0.25f, batch.getData()[FLOATS + fade], "The fade that comes in");
        assertEquals(-0.75f, batch.getData()[2 * FLOATS + fade], "The fade that goes out");
    }

    @Test
    void testBatchIsNotFadingInTheNextFrame() {
        batcher.add("tree", "program", "material", true, new Matrix4f(), 0.5f);
        batcher.clear();

        batcher.add("tree", "program", "material", true, new Matrix4f());

        assertFalse(batcher.getBatches().getFirst().isFading());
    }

}
