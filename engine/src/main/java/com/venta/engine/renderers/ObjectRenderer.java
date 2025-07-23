package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer extends AbstractRenderer<ObjectManager.ObjectEntity, ObjectView, ObjectRenderer.ObjectRenderContext> {
    private final MaterialRenderer materialRenderer;
    private final LightRenderer lightRenderer;

    @Override
    @SneakyThrows
    public void render(final ObjectView object) {
        final var programView = object.getProgram();
        if (programView != null) {
            final var context = getContext();
            if (context == null)
                throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

            glUseProgram(programView.entity.getIdAsInteger());

            glUniform4f(programView.entity.getUniformID("ambientLight"),
                    context.getAmbientLight().x(),
                    context.getAmbientLight().y(),
                    context.getAmbientLight().z(),
                    context.getAmbientLight().w());

            final var position = object.getPosition();
            glUniform3f(programView.entity.getUniformID("translation"), position.x(), position.y(), position.z());

            final var rotation = object.getRotation();
            glUniform3f(programView.entity.getUniformID("rotation"), rotation.x(), rotation.y(), rotation.z());

            final var scale = object.getScale();
            glUniform3f(programView.entity.getUniformID("scale"), scale.x(), scale.y(), scale.z());

            glUniformMatrix4fv(programView.entity.getUniformID("view"), false, context.getViewMatrixBuffer());
            glUniformMatrix4fv(programView.entity.getUniformID("projection"), false, context.getProjectionMatrixBuffer());

            try (final var _ = materialRenderer.withContext(MaterialRenderer.MaterialRenderContext.builder()
                            .useTextureDiffuse(programView.entity.getUniformID("useTextureDiffuse"))
                            .useTextureHeight(programView.entity.getUniformID("useTextureHeight"))
                            .textureDiffuse(programView.entity.getUniformID("textureDiffuse"))
                            .textureHeight(programView.entity.getUniformID("textureHeight"))
                    .build())) {
                materialRenderer.render(object.getMaterial());
            }

            final var lights = context.getLights();
            glUniform1i(programView.entity.getUniformID("lightCount"), lights.size());

            try (final var _ = lightRenderer.withContext(LightRenderer.LightRenderContext.builder()
                    .program(programView)
                    .build())) {
                lights.forEach(lightRenderer::render);
            }
        }

        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.entity.getFacets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @SuperBuilder
    @Getter(AccessLevel.PACKAGE)
    static final class ObjectRenderContext extends AbstractRenderContext {
        @NonNull
        private final FloatBuffer projectionMatrixBuffer;

        @NonNull
        private final FloatBuffer viewMatrixBuffer;

        @NonNull
        private final List<LightView> lights;

        @NonNull
        private final Vector4f ambientLight;

        @Override
        public void close() {
            //TODO: Possible optimization. Create render context just once and then just set the buffers
            MemoryUtil.memFree(projectionMatrixBuffer);
            MemoryUtil.memFree(viewMatrixBuffer);
        }
    }
}
