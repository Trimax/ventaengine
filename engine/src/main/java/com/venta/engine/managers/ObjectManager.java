package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.dto.ObjectDTO;
import com.venta.engine.model.view.ObjectView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ObjectManager extends AbstractManager<ObjectManager.ObjectEntity, ObjectView> {
    private final ResourceManager resourceManager;

    public ObjectView load(final String name) {
        log.info("Loading object {}", name);

        final var objectDTO = resourceManager.load(String.format("/objects/%s", name), ObjectDTO.class);

        final var vertices = objectDTO.getVerticesArray();
        final var facets = objectDTO.getFacesArray();

        final int vertexArrayObjectID = glGenVertexArrays();
        final int vertexBufferID = glGenBuffers();
        final int indexBufferID = glGenBuffers();

        glBindVertexArray(vertexArrayObjectID);

        // VBO
        final FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // EBO
        final IntBuffer indexBuffer = memAllocInt(facets.length);
        indexBuffer.put(facets).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        final int stride = 12 * Float.BYTES;

        // layout(location = 0) -> position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // layout(location = 1) -> normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // layout(location = 2) -> texture coordinates
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // layout(location = 3) -> color
        glVertexAttribPointer(3, 4, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);

        return store(new ObjectEntity(name, vertices, facets, vertexArrayObjectID, vertexBufferID, indexBufferID));
    }

    @Override
    protected ObjectView createView(final String id, final ObjectEntity entity) {
        return new ObjectView(id, entity);
    }

    @Override
    protected void destroy(final Couple<ObjectEntity, ObjectView> object) {
        log.info("Deleting object {}", object.entity().getName());
        glDeleteVertexArrays(object.entity().vertexArrayObjectID);
        glDeleteBuffers(object.entity().verticesBufferID);
        glDeleteBuffers(object.entity().facetsBufferID);
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity {
        private final String name;

        // Potentially we don't need to keep this in memory (or maybe use MeshCache)
        private final float[] vertices;
        private final int[] facets;

        @Getter
        @Setter
        private ProgramManager.ProgramEntity program;

        private final int vertexArrayObjectID;
        private final int verticesBufferID;
        private final int facetsBufferID;

        ObjectEntity(@NonNull final String name,
                     @NonNull final float[] vertices,
                     @NonNull final int[] facets,
                     final int vertexArrayObjectID,
                     final int verticesBufferID,
                     final int facetsBufferID) {
            super(0L);

            this.name = name;
            this.vertices = vertices;
            this.facets = facets;

            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;
            this.facetsBufferID = facetsBufferID;
        }
    }
}
