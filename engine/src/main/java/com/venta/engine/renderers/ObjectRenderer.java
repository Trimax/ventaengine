package com.venta.engine.renderers;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.WindowManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer implements AbstractRenderer<ObjectManager.ObjectEntity> {
    private final WindowManager windowManager;
    private final CameraManager cameraManager;

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

            try (final MemoryStack stack = MemoryStack.stackPush()) {
                final var fb = cameraManager.getCurrent().getViewMatrix().get(stack.mallocFloat(16));
                glUniformMatrix4fv(program.getUniformID("view"), false, fb);

                final var fb2 = windowManager.getCurrent().getProjectionMatrix().get(stack.mallocFloat(16));
                glUniformMatrix4fv(program.getUniformID("projection"), false, fb2);
            }
        }

        glBindVertexArray(object.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.getBakedObject().facets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
