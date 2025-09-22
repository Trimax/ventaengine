package io.github.trimax.venta.engine.helpers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.layouts.AbstractVertexLayout;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.utils.VertexLayoutUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeometryHelper {
    private final Memory memory;

    public void render(@NonNull final Geometry geometry) {
        if (!geometry.isRenderable())
            return;

        glBindVertexArray(geometry.objectID());

        if (geometry.hasFacets()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.facets().id());
            glDrawElements(GL_TRIANGLES, geometry.facets().length(), GL_UNSIGNED_INT, 0);
        }

        if (geometry.hasEdges()) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry.edges().id());
            glDrawElements(GL_LINES, geometry.edges().length(), GL_UNSIGNED_INT, 0);
        }

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
                    facets.length, Definitions.COUNT_VERTICES_PER_FACET);

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
                    edges.length, Definitions.COUNT_VERTICES_PER_EDGE);

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

        return new Buffer(memory.getBuffers().create(name), arrayLength / itemsPerElement, arrayLength);
    }
}
