package io.github.trimax.venta.engine.memory;

import static org.lwjgl.opengl.GL15C.*;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.layouts.InstanceVertexLayout;
import lombok.Getter;
import lombok.NonNull;

/**
 * The buffer of the copies of meshes that are drawn with instancing: the data of the batches of a frame is collected in
 * memory and uploaded in one piece, then every batch is drawn from its place in the buffer. The memory and the buffer
 * are kept between the frames and grow when a frame needs more.
 */
public final class InstanceBuffer {
    private static final int INITIAL_COPIES = 128;

    /** The GL buffer, 0 until the first upload */
    @Getter
    private int id;

    private FloatBuffer staging;

    /** Starts a new upload: what was appended before is forgotten */
    public void begin() {
        if (staging != null)
            staging.clear();
    }

    /**
     * Appends the copies of a batch.
     *
     * @param data   the copies as {@link InstanceVertexLayout#getFloatsCount()} floats each
     * @param length the number of floats to take from the data
     * @return the index of the first appended copy: the batch is drawn starting from it
     */
    public int append(@NonNull final float[] data, final int length) {
        final var floats = InstanceVertexLayout.getFloatsCount();
        if (staging == null)
            staging = MemoryUtil.memAllocFloat(Math.max(length, INITIAL_COPIES * floats));

        final var first = staging.position() / floats;
        if (staging.remaining() < length) {
            final var grown = MemoryUtil.memAllocFloat(Math.max(staging.capacity() * 2, staging.position() + length));
            staging.flip();
            grown.put(staging);

            MemoryUtil.memFree(staging);
            staging = grown;
        }

        staging.put(data, 0, length);
        return first;
    }

    /** Uploads everything appended since {@link #begin()}: the previous contents of the buffer are replaced */
    public void upload(@NonNull final Memory memory) {
        if (id == 0)
            id = memory.getBuffers().create("Buffer of the copies of meshes");

        glBindBuffer(GL_ARRAY_BUFFER, id);
        if (staging == null) {
            glBufferData(GL_ARRAY_BUFFER, 0L, GL_STREAM_DRAW);
            return;
        }

        staging.flip();
        glBufferData(GL_ARRAY_BUFFER, staging, GL_STREAM_DRAW);
        staging.clear();
    }

    /** The number of copies that were appended since {@link #begin()} */
    public int getCount() {
        return staging == null ? 0 : staging.position() / InstanceVertexLayout.getFloatsCount();
    }

    /** Frees the memory of the buffer, the GL buffer is deleted with the memory of the engine */
    public void destroy() {
        if (staging != null) {
            MemoryUtil.memFree(staging);
            staging = null;
        }
    }

    /** Frees the memory and deletes the GL buffer, the next upload creates it again */
    public void release(@NonNull final Memory memory) {
        destroy();

        if (id != 0) {
            memory.getBuffers().delete(id);
            id = 0;
        }
    }
}
