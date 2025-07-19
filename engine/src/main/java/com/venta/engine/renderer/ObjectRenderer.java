package com.venta.engine.renderer;

import com.venta.engine.annotations.Component;
import com.venta.engine.manager.ObjectManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUniform3f;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer implements AbstractRenderer<ObjectManager.ObjectEntity> {
    @Override
    public void render(final ObjectManager.ObjectEntity object) {
        final var program = object.getProgram();
        if (program != null) {
            glUseProgram(program.getIdAsInteger());

            final var position = object.getPosition();
            glUniform3f(program.getUniformID("translation"), position.x(), position.y(), position.z());

            final var rotation = object.getRotation();
            glUniform3f(program.getUniformID("rotation"), rotation.x(), rotation.y(), rotation.z());

            final var scale = object.getScale();
            glUniform3f(program.getUniformID("scale"), scale.x(),    scale.y(),    scale.z());
        }

        glBindVertexArray(object.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.getBakedObject().facets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
