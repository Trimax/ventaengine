package com.venta.engine.manager;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.math.Vector3;
import com.venta.engine.model.memory.BakedObject;
import com.venta.engine.model.parsing.VentaObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ObjectManager extends AbstractManager<ObjectManager.ObjectEntity> {
    private final ResourceManager resourceManager;

    public ObjectEntity load(final String name) {
        log.info("Loading object {}", name);

        final var parsedObject = resourceManager.load(String.format("/objects/%s", name), VentaObject.class);
        final var bakedObject = parsedObject.bake();

        final int vertexArrayObjectID = glGenVertexArrays();
        final int vertexBufferID = glGenBuffers();
        final int indexBufferID = glGenBuffers();

        glBindVertexArray(vertexArrayObjectID);

        // VBO
        final FloatBuffer vertexBuffer = memAllocFloat(bakedObject.vertices().length);
        vertexBuffer.put(bakedObject.vertices()).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // EBO
        final IntBuffer indexBuffer = memAllocInt(bakedObject.facets().length);
        indexBuffer.put(bakedObject.facets()).flip();

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

        return store(new ObjectEntity(generateID(), name, parsedObject, bakedObject, vertexArrayObjectID, vertexBufferID, indexBufferID));
    }

    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Deleting object {}", object.getName());
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity {
        private final String name;
        private final VentaObject object;
        private final BakedObject bakedObject;

        @Getter
        @Setter
        private ProgramManager.ProgramEntity program;

        //TODO: Those things should be mutable
        @Getter
        @Setter
        @NonNull
        private Vector3 position = new Vector3(0.f, 0.f, 0.f);

        @Getter
        @Setter
        @NonNull
        private Vector3 rotation = new Vector3(0.f, 0.f, 0.f);

        @Getter
        @Setter
        @NonNull
        private Vector3 scale = new Vector3(1.f, 1.f, 1.f);

        private final int vertexArrayObjectID;
        private final int verticesBufferID;
        private final int facetsBufferID;

        ObjectEntity(final long id,
                     @NonNull final String name,
                     @NonNull final VentaObject object,
                     @NonNull final BakedObject bakedObject,
                     @NonNull final int vertexArrayObjectID,
                     @NonNull final int verticesBufferID,
                     @NonNull final int facetsBufferID) {
            super(id);

            this.name = name;
            this.object = object;
            this.bakedObject = bakedObject;

            this.vertexArrayObjectID = vertexArrayObjectID;
            this.verticesBufferID = verticesBufferID;
            this.facetsBufferID = facetsBufferID;
        }
    }
}
