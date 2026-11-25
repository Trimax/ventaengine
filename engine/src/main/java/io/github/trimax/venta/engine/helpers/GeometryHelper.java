package io.github.trimax.venta.engine.helpers;

import static org.lwjgl.opengl.GL14C.glMultiDrawElements;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.PointerBuffer;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsBuffer;
import io.github.trimax.venta.engine.layouts.AbstractVertexLayout;
import io.github.trimax.venta.engine.layouts.InstanceVertexLayout;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.utils.VertexLayoutUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeometryHelper {
    private final Memory memory;

    /** The calls that draw geometry: the ones of the frame in progress, and the ones of the last frame that ended */
    private int drawCalls;
    private volatile int lastFrameDrawCalls;

    /** Starts counting the draw calls of a new frame */
    public void startFrame() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    /** The number of calls that drew geometry (objects, terrain, shadows) in the last frame that ended */
    public int getDrawCalls() {
        return lastFrameDrawCalls;
    }

    public void render(@NonNull final Geometry geometry) {
        if (!geometry.isRenderable())
            return;

        glBindVertexArray(geometry.objectID());

        if (geometry.hasFacets()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.facets().id());
            glDrawElements(GL_TRIANGLES, geometry.facets().length(), GL_UNSIGNED_INT, 0);
            drawCalls++;
        }

        if (geometry.hasEdges()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.edges().id());
            glDrawElements(GL_LINES, geometry.edges().length(), GL_UNSIGNED_INT, 0);
            drawCalls++;
        }

        glBindVertexArray(0);
    }

    /**
     * Draws parts of the facets of a geometry with one call: the chunks of a terrain, each with the indices of its level of detail.
     *
     * @param counts  how many indices every part has
     * @param offsets where the indices of every part start in the facets, in bytes
     * @param parts   how many parts there are, from the start of the buffers
     */
    public void renderParts(@NonNull final Geometry geometry, @NonNull final IntBuffer counts, @NonNull final PointerBuffer offsets, final int parts) {
        if (!geometry.isRenderable() || !geometry.hasFacets() || parts <= 0)
            return;

        glBindVertexArray(geometry.objectID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.facets().id());

        counts.limit(parts);
        offsets.limit(parts);
        glMultiDrawElements(GL_TRIANGLES, counts, GL_UNSIGNED_INT, offsets);
        counts.clear();
        offsets.clear();
        drawCalls++;

        glBindVertexArray(0);
    }

    /**
     * Draws many copies of a geometry with one call. The attributes of the copies ({@link InstanceVertexLayout}) are
     * read from the buffer, which holds the data of {@code instances} copies; they are attached to the geometry for
     * this call only, so the geometry can still be drawn on its own.
     *
     * @param first the index of the first copy of the geometry in the buffer, which may hold the copies of several batches
     */
    public void renderInstanced(@NonNull final Geometry geometry, final int instanceBufferID, final int first, final int instances) {
        if (!geometry.isRenderable() || instances <= 0)
            return;

        glBindVertexArray(geometry.objectID());
        glBindBuffer(GL_ARRAY_BUFFER, instanceBufferID);

        final var stride = InstanceVertexLayout.getStride();
        long offset = (long) first * stride;
        for (final var attribute : InstanceVertexLayout.values()) {
            // A matrix takes several locations: one for every column
            final var size = attribute.getSize() / attribute.getColumns();
            for (int column = 0; column < attribute.getColumns(); column++) {
                final var location = attribute.getLocationID() + column;
                glEnableVertexAttribArray(location);
                glVertexAttribPointer(location, size, GL_FLOAT, false, stride, offset + (long) column * size * Float.BYTES);
                glVertexAttribDivisor(location, 1);
            }

            offset += (long) Float.BYTES * attribute.getSize();
        }

        if (geometry.hasFacets()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.facets().id());
            glDrawElementsInstanced(GL_TRIANGLES, geometry.facets().length(), GL_UNSIGNED_INT, 0, instances);
            drawCalls++;
        }

        if (geometry.hasEdges()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.edges().id());
            glDrawElementsInstanced(GL_LINES, geometry.edges().length(), GL_UNSIGNED_INT, 0, instances);
            drawCalls++;
        }

        for (final var attribute : InstanceVertexLayout.values())
            for (int column = 0; column < attribute.getColumns(); column++)
                glDisableVertexAttribArray(attribute.getLocationID() + column);

        glBindVertexArray(0);
    }

    public <E extends Enum<E> & AbstractVertexLayout> Geometry create(@NonNull final String name,
                                                                      @NonNull final Class<E> layout,
                                                                      final float[] vertices,
                                                                      final int[] facets,
                                                                      final int[] edges) {
        final var objectID = memory.getVertexArrays().create("VAO buffer `%s` (%s)", name, layout.getSimpleName());

        glBindVertexArray(objectID);

        // Vertices
        Buffer bufferVertices = null;
        if (ArrayUtils.isNotEmpty(vertices)) {
            final FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();

            bufferVertices = allocateBuffer(String.format("Vertices buffer `%s`", name), vertices.length, VertexLayoutUtil.calculateTotalFloats(layout));

            glBindBuffer(GL_ARRAY_BUFFER, bufferVertices.id());
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            memFree(vertexBuffer);
        }

        // Facets
        Buffer bufferFacets = null;
        if (ArrayUtils.isNotEmpty(facets)) {
            final IntBuffer indexBuffer = memAllocInt(facets.length);
            indexBuffer.put(facets).flip();

            bufferFacets = allocateBuffer(String.format("Facets buffer `%s` (%s)", name, layout.getSimpleName()),
                    facets.length, DefinitionsBuffer.COUNT_VERTICES_PER_FACET);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferFacets.id());
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        // Edges
        Buffer bufferEdges = null;
        if (ArrayUtils.isNotEmpty(edges)) {
            final IntBuffer indexBuffer = memAllocInt(edges.length);
            indexBuffer.put(edges).flip();

            bufferEdges = allocateBuffer(String.format("Edges buffer `%s` (%s)", name, layout.getSimpleName()),
                    edges.length, DefinitionsBuffer.COUNT_VERTICES_PER_EDGE);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferEdges.id());
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        VertexLayoutUtil.bind(layout);

        glBindVertexArray(0);

        return new Geometry(objectID, bufferVertices, bufferFacets, bufferEdges);
    }

    public void delete(@NonNull final Geometry geometry) {
        memory.getVertexArrays().delete(geometry.objectID());

        Optional.ofNullable(geometry.vertices()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
        Optional.ofNullable(geometry.facets()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
        Optional.ofNullable(geometry.edges()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
    }

    private Buffer allocateBuffer(@NonNull final String name, final int arrayLength, final int itemsPerElement) {
        if (arrayLength % itemsPerElement != 0)
            log.warn("Buffer `{}` has incorrect length: {} (must be a product of {})", name, arrayLength, itemsPerElement);

        return new Buffer(memory.getBuffers().create("Buffer %s", name), arrayLength / itemsPerElement, arrayLength);
    }
}
