package io.github.trimax.venta.engine.helpers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeometryHelper {
    private final Memory memory;

    public Geometry create(@NonNull final String name,
                           final int countVertices, final int floatsPerVertex,
                           final int countFacets, final int floatsPerFacet,
                           final int countEdges, final int floatsPerEdge) {

        return new Geometry(memory.getVertexArrays().create("Vertex array buffer `%s`", name),
                allocateBuffer(String.format("Vertices buffer `%s`", name), countVertices, floatsPerVertex),
                allocateBuffer(String.format("Facets buffer `%s`", name), countFacets, floatsPerFacet),
                allocateBuffer(String.format("Edges buffer `%s`", name), countEdges, floatsPerEdge));
    }

    public void delete(@NonNull final Geometry geometry) {
        memory.getVertexArrays().delete(geometry.objectID());

        Optional.ofNullable(geometry.vertices()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
        Optional.ofNullable(geometry.facets()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
        Optional.ofNullable(geometry.edges()).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
    }

    private Buffer allocateBuffer(@NonNull final String name, final int countItems, final int floatsPerItem) {
        if (countItems * floatsPerItem <= 0)
            return null;

        return new Buffer(memory.getBuffers().create(name), countItems, countItems * floatsPerItem);
    }
}
