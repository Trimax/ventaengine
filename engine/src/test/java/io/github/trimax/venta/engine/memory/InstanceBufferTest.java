package io.github.trimax.venta.engine.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL15C.glIsBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.layouts.InstanceVertexLayout;
import io.github.trimax.venta.engine.shaders.GLTestContext;

final class InstanceBufferTest {
    private static final int FLOATS = InstanceVertexLayout.getFloatsCount();

    private final InstanceBuffer buffer = new InstanceBuffer();

    @AfterEach
    void free() {
        buffer.destroy();
    }

    private static float[] copies(final int count, final float value) {
        final var data = new float[count * FLOATS];
        java.util.Arrays.fill(data, value);
        return data;
    }

    @Test
    void testBatchesFollowEachOther() {
        buffer.begin();

        assertEquals(0, buffer.append(copies(3, 1), 3 * FLOATS));
        assertEquals(3, buffer.append(copies(2, 2), 2 * FLOATS));
        assertEquals(5, buffer.append(copies(1, 3), FLOATS));
        assertEquals(6, buffer.getCount());
    }

    @Test
    void testOnlyTheLengthIsTaken() {
        buffer.begin();
        buffer.append(copies(10, 1), 2 * FLOATS);

        assertEquals(2, buffer.getCount(), "The tail of the array of a batch is not used");
    }

    @Test
    void testBeginForgetsThePreviousFrame() {
        buffer.begin();
        buffer.append(copies(4, 1), 4 * FLOATS);

        buffer.begin();

        assertEquals(0, buffer.getCount());
        assertEquals(0, buffer.append(copies(1, 1), FLOATS));
    }

    @Test
    void testGrowthKeepsWhatWasAppended() {
        buffer.begin();
        for (int i = 0; i < 1000; i++)
            assertEquals(i, buffer.append(copies(1, i), FLOATS));

        assertEquals(1000, buffer.getCount());
    }

    @Test
    void testABatchBiggerThanTheBufferFits() {
        buffer.begin();
        buffer.append(copies(1, 1), FLOATS);

        assertEquals(1, buffer.append(copies(5000, 2), 5000 * FLOATS));
        assertEquals(5001, buffer.getCount());
    }

    @Test
    void testEmptyBufferHasNothing() {
        assertEquals(0, buffer.getCount());
        assertEquals(0, buffer.getId(), "The GL buffer is created with the first upload");
        buffer.begin();
        assertEquals(0, buffer.getCount());
    }

    @Test
    void testReleaseDeletesTheGlBuffer() throws Exception {
        GLTestContext.require();

        final var memory = TestMemory.create();
        buffer.begin();
        buffer.append(copies(2, 1), 2 * FLOATS);
        buffer.upload(memory);

        final var id = buffer.getId();
        assertNotEquals(0, id, "The first upload creates the GL buffer");
        assertTrue(glIsBuffer(id));

        buffer.release(memory);

        assertFalse(glIsBuffer(id), "It is deleted, or the memory of the engine reports it at the shutdown");
        assertEquals(0, buffer.getId());
        assertEquals(0, buffer.getCount());
    }

    @Test
    void testReleaseTwiceAndWithoutAnUploadIsHarmless() throws Exception {
        final var memory = TestMemory.create();

        assertDoesNotThrow(() -> buffer.release(memory), "Nothing was made yet");
        assertDoesNotThrow(() -> buffer.release(memory));
    }

    @Test
    void testUploadAfterReleaseMakesTheBufferAgain() throws Exception {
        GLTestContext.require();

        final var memory = TestMemory.create();
        buffer.begin();
        buffer.append(copies(1, 1), FLOATS);
        buffer.upload(memory);
        buffer.release(memory);

        buffer.begin();
        buffer.append(copies(1, 2), FLOATS);
        buffer.upload(memory);

        assertTrue(glIsBuffer(buffer.getId()), "The next frame has a buffer again");
        buffer.release(memory);
    }
}
