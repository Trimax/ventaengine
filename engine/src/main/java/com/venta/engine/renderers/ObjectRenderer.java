package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.WindowManager;
import com.venta.engine.model.view.ObjectView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer implements AbstractRenderer<ObjectManager.ObjectEntity, ObjectView> {
    private final WindowManager windowManager;
    private final CameraManager cameraManager;

    @Override
    public void render(final ObjectView object) {
        final var programView = object.getProgram();
        if (programView != null) {
            glUseProgram(programView.entity.getIdAsInteger());

            final var position = object.getPosition();
            glUniform3f(programView.entity.getUniformID("translation"), position.x(), position.y(), position.z());

            final var rotation = object.getRotation();
            glUniform3f(programView.entity.getUniformID("rotation"), rotation.x(), rotation.y(), rotation.z());

            final var scale = object.getScale();
            glUniform3f(programView.entity.getUniformID("scale"), scale.x(),    scale.y(),    scale.z());

            try (final MemoryStack stack = MemoryStack.stackPush()) {
                final var fb = cameraManager.getCurrent().entity.getViewMatrix().get(stack.mallocFloat(16));
                glUniformMatrix4fv(programView.entity.getUniformID("view"), false, fb);

                final var fb2 = windowManager.getCurrent().entity.getProjectionMatrix().get(stack.mallocFloat(16));
                glUniformMatrix4fv(programView.entity.getUniformID("projection"), false, fb2);
            }
        }

        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.entity.getFacets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
