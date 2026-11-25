package io.github.trimax.venta.engine.helpers;

import java.nio.IntBuffer;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.terrain.TerrainChunks;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Chooses the level of detail of the chunks of a terrain by the distance to the camera, once in a frame, and draws the chunks that
 * a view sees, with one call. The shadow maps and the reflection draw the levels the camera chose, so the geometry is the same in
 * every pass.
 *
 * <p>The nearest chunks have the finest level (the step of one cell), and every level starts at twice the distance of the one
 * before it, counted from the distance of the first, which is a bit more than the size of a chunk.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerrainChunkHelper {
    /** The first level change is at this many sizes of a chunk, when the terrain doesn't say */
    static final float DEFAULT_LEVEL_DISTANCE_CHUNKS = 1.5f;

    private final GeometryHelper geometryHelper;

    private final Vector3f minimum = new Vector3f();
    private final Vector3f maximum = new Vector3f();
    private final Vector3f scale = new Vector3f();

    private IntBuffer counts;
    private PointerBuffer offsets;

    /** The triangles of the terrain drawn in the frame in progress, and in the last frame that ended */
    private int triangles;
    private volatile int lastFrameTriangles;

    /** Starts counting the triangles of a new frame */
    public void startFrame() {
        lastFrameTriangles = triangles;
        triangles = 0;
    }

    /** The number of triangles that the terrain had drawn (in all the passes: shadows, reflection and scene) in the last frame that ended */
    public int getTriangles() {
        return lastFrameTriangles;
    }

    /**
     * The level of detail of a level for a distance: 0 up to the base distance, then one more for every doubling of it.
     *
     * @param distance the distance from the camera to the chunk
     * @param base     the distance the first change of the level is at
     * @param levels   how many levels there are
     */
    public static int getLevel(final float distance, final float base, final int levels) {
        if (!(distance >= base) || base <= 0.f)
            return 0;

        return Math.min(levels - 1, 1 + (int) Math.floor(Math.log(distance / base) / Math.log(2.0)));
    }

    /**
     * Chooses the level of detail of every chunk.
     *
     * @param model         the matrix of the terrain
     * @param camera        where the camera is
     * @param factor        the range of the heights of the terrain
     * @param levelDistance the distance (in the world) the first level changes at, 0 for a bit more than the size of a chunk
     */
    public void update(@NonNull final TerrainChunks chunks, @NonNull final Matrix4fc model, @NonNull final Vector3fc camera,
                       final float factor, final float levelDistance) {
        final var layout = chunks.getLayout();
        model.getScale(scale);

        final var base = levelDistance > 0.f ? levelDistance
                : DEFAULT_LEVEL_DISTANCE_CHUNKS * Math.max(layout.getChunkSizeX() * Math.abs(scale.x), layout.getChunkSizeZ() * Math.abs(scale.z));

        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
            final var box = chunks.getBox(chunk, factor);
            model.transformAab(box.min(), box.max(), minimum, maximum);

            final var distance = (float) Math.sqrt(squaredDistance(camera.x(), minimum.x, maximum.x)
                    + squaredDistance(camera.y(), minimum.y, maximum.y) + squaredDistance(camera.z(), minimum.z, maximum.z));
            chunks.setLevel(chunk, getLevel(distance, base, layout.getLevels()));
        }
    }

    /**
     * Draws the chunks that a view sees: the ones of the box that is in it, each with its level, with one call.
     *
     * @param model  the matrix of the terrain
     * @param factor the range of the heights of the terrain
     * @param culler the view, which has the planes it cuts by set
     */
    public void draw(@NonNull final TerrainChunks chunks, @NonNull final Matrix4fc model, final float factor, @NonNull final FrustumHelper culler) {
        final var parts = select(chunks, model, factor, culler);

        geometryHelper.renderParts(chunks.getGeometry(), counts, offsets, parts);
    }

    /**
     * Chooses the chunks that a view sees and puts their ranges of indices in the buffers of the draw call.
     *
     * @return how many chunks there are in the buffers
     */
    int select(@NonNull final TerrainChunks chunks, @NonNull final Matrix4fc model, final float factor, @NonNull final FrustumHelper culler) {
        final var layout = chunks.getLayout();
        reserve(layout.getChunkCount());

        var parts = 0;
        for (int chunk = 0; chunk < layout.getChunkCount(); chunk++) {
            if (!culler.isVisible(chunks.getBox(chunk, factor), model, 0.f))
                continue;

            final var level = chunks.getLevel(chunk);
            counts.put(parts, layout.getCount(chunk, level));
            offsets.put(parts, (long) layout.getOffset(chunk, level) * Integer.BYTES);
            triangles += layout.getCount(chunk, level) / 3;
            parts++;
        }

        return parts;
    }

    /** How many indices the part of the last selection has */
    int getSelectedCount(final int part) {
        return counts.get(part);
    }

    /** Where the indices of the part of the last selection start, in bytes */
    long getSelectedOffset(final int part) {
        return offsets.get(part);
    }

    /** Releases the buffers of the draw calls, the next frame makes them again */
    public void release() {
        if (counts != null) {
            MemoryUtil.memFree(counts);
            MemoryUtil.memFree(offsets);
            counts = null;
            offsets = null;
        }
    }

    private void reserve(final int chunks) {
        if (counts != null && counts.capacity() >= chunks)
            return;

        release();
        counts = MemoryUtil.memAllocInt(chunks);
        offsets = MemoryUtil.memAllocPointer(chunks);
    }

    /** The squared distance of a coordinate from a range: 0 inside of it */
    private static float squaredDistance(final float point, final float low, final float high) {
        final var outside = Math.max(Math.max(low - point, point - high), 0.f);
        return outside * outside;
    }
}
