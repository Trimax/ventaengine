package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.model.view.ObjectView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer extends AbstractRenderer<ObjectManager.ObjectEntity, ObjectView> {
    @Override
    public void render(final ObjectView object) {
        final var programView = object.getProgram();
        if (programView != null) {
            final var context = getContext();
            if (context == null)
                throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

            glUseProgram(programView.entity.getIdAsInteger());

            final var position = object.getPosition();
            glUniform3f(programView.entity.getUniformID("translation"), position.x(), position.y(), position.z());

            final var rotation = object.getRotation();
            glUniform3f(programView.entity.getUniformID("rotation"), rotation.x(), rotation.y(), rotation.z());

            final var scale = object.getScale();
            glUniform3f(programView.entity.getUniformID("scale"), scale.x(), scale.y(), scale.z());

            glUniformMatrix4fv(programView.entity.getUniformID("view"), false, context.getViewMatrixBuffer());
            glUniformMatrix4fv(programView.entity.getUniformID("projection"), false, context.getProjectionMatrixBuffer());
        }

        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.entity.getFacets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
