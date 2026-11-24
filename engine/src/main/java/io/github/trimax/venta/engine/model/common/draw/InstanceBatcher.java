package io.github.trimax.venta.engine.model.common.draw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4fc;

import io.github.trimax.venta.engine.layouts.InstanceVertexLayout;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Collects the copies of meshes of a frame into batches: the copies that share a shape, a program, a material and the
 * lighting make one batch, which is drawn with one call. The matrices of the copies are stored in the order of
 * {@link InstanceVertexLayout}, so a batch can be uploaded to the buffer of the copies as it is.
 *
 * <p>It is filled for every frame, so one instance keeps its batches and arrays and reuses them: collecting allocates
 * nothing once they have grown to the size of the scene. The shapes, programs and materials are compared by reference.
 *
 * @param <S> the shape of the copies (a mesh)
 * @param <P> the program they are drawn with
 * @param <M> the material they are drawn with
 */
public final class InstanceBatcher<S, P, M> {
    private static final int INITIAL_CAPACITY = 16;

    /** The first batch of every shape: the batches of a shape are linked, there are few of them (usually one) */
    private final Map<S, Batch<S, P, M>> heads = new IdentityHashMap<>();

    /** The batches of the frame in the order of their creation */
    private final List<Batch<S, P, M>> active = new ArrayList<>();

    /** The batches that were created once: the first ones are in use, the others wait for a frame that needs them */
    private final List<Batch<S, P, M>> created = new ArrayList<>();

    private final Matrix3f normalMatrix = new Matrix3f();

    /**
     * The batches of the frame, valid until the next {@link #clear()}; the list must not be changed
     */
    public List<Batch<S, P, M>> getBatches() {
        return active;
    }

    /** The number of copies in all the batches of the frame */
    public int getCopiesCount() {
        var count = 0;
        for (final var batch : active)
            count += batch.getCount();

        return count;
    }

    /** Forgets the copies of the frame: the batches are kept for the next one, but they don't keep the scene alive */
    public void clear() {
        for (final var batch : active)
            batch.reset();

        active.clear();
        heads.clear();
    }

    /**
     * Adds a copy of a shape.
     *
     * @param model the model matrix of the copy, its normal matrix is computed here
     */
    public void add(@NonNull final S shape, final P program, final M material, final boolean lit, @NonNull final Matrix4fc model) {
        final var head = heads.get(shape);

        var batch = head;
        while (batch != null && !batch.matches(program, material, lit))
            batch = batch.next;

        if (batch == null) {
            batch = obtain();
            batch.begin(shape, program, material, lit, head);
            heads.put(shape, batch);
            active.add(batch);
        }

        model.normal(normalMatrix);
        batch.append(model, normalMatrix);
    }

    private Batch<S, P, M> obtain() {
        if (active.size() == created.size())
            created.add(new Batch<>());

        return created.get(active.size());
    }

    /** The copies of one shape that are drawn in the same way */
    @Getter
    public static final class Batch<S, P, M> {
        private S shape;
        private P program;
        private M material;
        private boolean lit;

        /** The number of copies */
        private int count;

        /**
         * The copies: {@link InstanceVertexLayout#getFloatsCount()} floats of every one, the first {@link #getLength()} are used
         */
        private float[] data = new float[INITIAL_CAPACITY * InstanceVertexLayout.getFloatsCount()];

        @Getter(AccessLevel.NONE)
        private Batch<S, P, M> next;

        private Batch() {
        }

        /**
         * The number of floats of the copies in {@link #getData()}
         */
        public int getLength() {
            return count * InstanceVertexLayout.getFloatsCount();
        }

        private boolean matches(final P program, final M material, final boolean lit) {
            return this.program == program && this.material == material && this.lit == lit;
        }

        private void begin(final S shape, final P program, final M material, final boolean lit, final Batch<S, P, M> next) {
            this.shape = shape;
            this.program = program;
            this.material = material;
            this.lit = lit;
            this.next = next;
            this.count = 0;
        }

        private void append(final Matrix4fc model, final Matrix3f normal) {
            final var floats = InstanceVertexLayout.getFloatsCount();
            if ((count + 1) * floats > data.length)
                data = Arrays.copyOf(data, data.length * 2);

            final var offset = count * floats;
            model.get(data, offset);
            normal.get(data, offset + InstanceVertexLayout.MatrixModel.getSize());
            count++;
        }

        private void reset() {
            shape = null;
            program = null;
            material = null;
            next = null;
            count = 0;
        }
    }
}
