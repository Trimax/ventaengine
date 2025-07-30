package com.venta.engine.managers;

import static com.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.dto.MeshDTO;
import com.venta.engine.model.view.MaterialView;
import com.venta.engine.model.view.MeshView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshManager extends AbstractManager<MeshManager.MeshEntity, MeshView> {
    private final ResourceManager resourceManager;

    public MeshView load(final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading mesh {}", name);

        final var objectDTO = resourceManager.load(String.format("/meshes/%s.json", name), MeshDTO.class);

        final var vertices = objectDTO.getVerticesArray();

        final int vertexArrayObjectID = glGenVertexArrays();
        final int vertexBufferID = glGenBuffers();
        final int facetsBufferID = glGenBuffers();
        final int edgesBufferID = glGenBuffers();

        glBindVertexArray(vertexArrayObjectID);

        // Vertices
        final FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // Facets
        if (objectDTO.hasFacets()) {
            final var facets = objectDTO.getFacesArray();
            final IntBuffer indexBuffer = memAllocInt(facets.length);
            indexBuffer.put(facets).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facetsBufferID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        // Edges
        if (objectDTO.hasEdges()) {
            final var edges = objectDTO.getEdgesArray();
            final IntBuffer indexBuffer = memAllocInt(edges.length);
            indexBuffer.put(edges).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, edgesBufferID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
        }

        final int stride = COUNT_FLOATS_PER_VERTEX * Float.BYTES;

        // layout(location = 0) -> position
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_POSITION, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_POSITION);

        // layout(location = 1) -> normal
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_NORMAL, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_NORMAL);

        // layout(location = 2) -> tangent
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_TANGENT, 3, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_TANGENT);

        // layout(location = 3) -> bitangent
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_BITANGENT, 3, GL_FLOAT, false, stride, 9 * Float.BYTES);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_BITANGENT);

        // layout(location = 4) -> texture coordinates
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_TEXTURE_COORDINATES, 2, GL_FLOAT, false, stride, 12 * Float.BYTES);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_TEXTURE_COORDINATES);

        // layout(location = 5) -> color
        glVertexAttribPointer(VERTEX_ATTRIBUTE_INDEX_COLOR, 4, GL_FLOAT, false, stride, 14 * Float.BYTES);
        glEnableVertexAttribArray(VERTEX_ATTRIBUTE_INDEX_COLOR);

        glBindVertexArray(0);

        return store(new MeshEntity(name, vertices.length, objectDTO.getFacetsArrayLength(),
                objectDTO.getEdgesArrayLength(), vertexArrayObjectID, vertexBufferID, facetsBufferID, edgesBufferID));
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    protected void destroy(final MeshEntity object) {
        log.info("Destroying mesh {} ({})", object.getID(), object.getName());
        glDeleteVertexArrays(object.vertexArrayObjectID);
        glDeleteBuffers(object.verticesBufferID);
        glDeleteBuffers(object.facetsBufferID);
        glDeleteBuffers(object.edgesBufferID);
    }

    @Getter
    public static final class MeshEntity extends AbstractEntity implements MeshView {
        private final int verticesCount;
        private final int facetsCount;
        private final int edgesCount;

        private final int vertexArrayObjectID;
        private final int verticesBufferID;
        private final int facetsBufferID;
        private final int edgesBufferID;

        private MaterialManager.MaterialEntity material;

        MeshEntity(@NonNull final String name,
                   final int verticesCount,
                   final int facetsCount,
                   final int edgesCount,
                   final int vertexArrayObjectID,
                   final int verticesBufferID,
                   final int facetsBufferID,
                   final int edgesBufferID) {
            super(name);

            this.verticesCount = verticesCount;
            this.facetsCount = facetsCount;
            this.edgesCount = edgesCount;

            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;
            this.facetsBufferID = facetsBufferID;
            this.edgesBufferID = edgesBufferID;
        }

        @Override
        public void setMaterial(final MaterialView material) {
            if (material instanceof MaterialManager.MaterialEntity entity)
                this.material = entity;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class MeshAccessor extends AbstractAccessor {}
}
