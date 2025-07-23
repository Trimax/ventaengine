package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer extends AbstractRenderer<ObjectManager.ObjectEntity, ObjectView, ObjectRenderer.ObjectRenderContext> {
    private final MaterialRenderer materialRenderer;
    private final LightRenderer lightRenderer;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

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

            glUniformMatrix4fv(programView.entity.getUniformID("viewProjectionMatrix"), false, context.getViewProjectionMatrixBuffer());

            try (final var _ = materialRenderer.getContext()
                    .withTextureDiffuse(programView.entity.getUniformID("textureDiffuse"), programView.entity.getUniformID("useTextureDiffuse"))
                    .withTextureHeight(programView.entity.getUniformID("textureHeight"), programView.entity.getUniformID("useTextureHeight"))) {
                materialRenderer.render(object.getMaterial());
            }

            final var lights = context.getLights();
            glUniform1i(programView.entity.getUniformID("lightCount"), lights.size());

            for (int lightID = 0; lightID < lights.size(); lightID++) {
                final var prefix = "lights[" + lightID + "]";
                try (final var _ = lightRenderer.getContext()
                        .withType(programView.entity.getUniformID(prefix + ".type"))
                        .withColor(programView.entity.getUniformID(prefix + ".color"))
                        .withEnabled(programView.entity.getUniformID(prefix + ".enabled"))
                        .withPosition(programView.entity.getUniformID(prefix + ".position"))
                        .withDirection(programView.entity.getUniformID(prefix + ".direction"))
                        .withIntensity(programView.entity.getUniformID(prefix + ".intensity"))
                        .withCastShadows(programView.entity.getUniformID(prefix + ".castShadows"))
                        .withAttenuationLinear(programView.entity.getUniformID(prefix + ".attenuation.linear"))
                        .withAttenuationConstant(programView.entity.getUniformID(prefix + ".attenuation.constant"))
                        .withAttenuationQuadratic(programView.entity.getUniformID(prefix + ".attenuation.quadratic"))) {
                    lightRenderer.render(lights.get(lightID));
                }
            }
        }

        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.entity.getFacets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    static final class ObjectRenderContext extends AbstractRenderContext {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final List<LightView> lights = new ArrayList<>();
        private final Vector4f ambientLight = new Vector4f();

        public ObjectRenderContext withViewProjectionMatrix(final FloatBuffer buffer) {
            this.viewProjectionMatrixBuffer.put(buffer).flip();
            return this;
        }

        public ObjectRenderContext withScene(final SceneView scene) {
            lights.addAll(scene.getLights());
            ambientLight.set(scene.getAmbientLight());
            return this;
        }

        @Override
        public void close() {
            viewProjectionMatrixBuffer.clear();
            ambientLight.set(0.f);
            lights.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
        }
    }
}
