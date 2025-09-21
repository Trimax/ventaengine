package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.factories.MeshParserFactory;
import io.github.trimax.venta.engine.layouts.MeshVertexLayout;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.registries.MeshRegistry;
import io.github.trimax.venta.engine.utils.LayoutUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static io.github.trimax.venta.engine.definitions.Definitions.COUNT_VERTICES_PER_EDGE;
import static io.github.trimax.venta.engine.definitions.Definitions.COUNT_VERTICES_PER_FACET;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshRegistryImplementation
        extends AbstractRegistryImplementation<MeshEntityImplementation, MeshEntity, Void>
        implements MeshRegistry {
    private final MeshParserFactory meshParserFactory;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    protected MeshEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading mesh {}", resourcePath);

        return createMesh(resourcePath, meshParserFactory.get(resourcePath).parse(String.format("/meshes/%s", resourcePath)));
    }

    private MeshEntityImplementation createMesh(final String resourcePath, final MeshDTO meshDTO) {
        final var vertices = meshDTO.getVerticesArray();

        final int vertexArrayObjectID = memory.getVertexArrays().create("Mesh %s vertex array buffer", resourcePath);
        final int vertexBufferID = memory.getBuffers().create("Mesh %s vertex buffer", resourcePath);
        final int facetsBufferID = memory.getBuffers().create("Mesh %s face buffer", resourcePath);
        final int edgesBufferID = memory.getBuffers().create("Mesh %s edge buffer", resourcePath);

        glBindVertexArray(vertexArrayObjectID);

        // Vertices
        final FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // Facets
        if (meshDTO.hasFacets()) {
            final var facets = meshDTO.getFacesArray();
            final IntBuffer indexBuffer = memAllocInt(facets.length);
            indexBuffer.put(facets).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facetsBufferID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        // Edges
        if (meshDTO.hasEdges()) {
            final var edges = meshDTO.getEdgesArray();
            final IntBuffer indexBuffer = memAllocInt(edges.length);
            indexBuffer.put(edges).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, edgesBufferID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        LayoutUtil.bind(MeshVertexLayout.class);

        glBindVertexArray(0);

        return abettor.createMesh(vertices.length, meshDTO.getFacetsArrayLength(), meshDTO.getEdgesArrayLength(),
                new Geometry(vertexArrayObjectID,
                        new Buffer(vertexBufferID, vertices.length / MeshVertexLayout.getFloatsCount(), vertices.length),
                        new Buffer(facetsBufferID, meshDTO.getFacetsArrayLength() / COUNT_VERTICES_PER_FACET, meshDTO.getFacetsArrayLength()),
                        new Buffer(edgesBufferID, meshDTO.getEdgesArrayLength() / COUNT_VERTICES_PER_EDGE, meshDTO.getEdgesArrayLength())),
                BoundingBox.of(meshDTO));
    }

    @Override
    protected void unload(@NonNull final MeshEntityImplementation entity) {
        log.info("Unloading mesh {}", entity.getID());

        memory.getVertexArrays().delete(entity.getGeometry().objectID());
        unload(entity.getGeometry().vertices());
        unload(entity.getGeometry().facets());
        unload(entity.getGeometry().edges());
    }

    private void unload(final Buffer buffer) {
        Optional.ofNullable(buffer).map(Buffer::id).ifPresent(memory.getBuffers()::delete);
    }
}