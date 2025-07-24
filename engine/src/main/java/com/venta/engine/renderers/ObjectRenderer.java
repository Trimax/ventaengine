package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
        if (programView == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(programView.entity.getIdAsInteger());
        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        glUniform4f(programView.entity.getUniformID("ambientLight"), context.getAmbientLight().x(),
                context.getAmbientLight().y(), context.getAmbientLight().z(), context.getAmbientLight().w());

        glUniformMatrix4fv(programView.entity.getUniformID("matrixViewProjection"), false,
                context.getViewProjectionMatrixBuffer());
        glUniformMatrix3fv(programView.entity.getUniformID("matrixNormal"), false, context.getNormalMatrixBuffer());
        glUniformMatrix4fv(programView.entity.getUniformID("matrixModel"), false, context.getModelMatrixBuffer());

        try (final var _ = materialRenderer.getContext()
                .withTextureDiffuse(programView.entity.getUniformID("textureDiffuse"),
                        programView.entity.getUniformID("useTextureDiffuse"))
                .withTextureHeight(programView.entity.getUniformID("textureHeight"),
                        programView.entity.getUniformID("useTextureHeight"))) {
            materialRenderer.render(object.getMaterial());
        }

        final var lights = context.getLights();
        glUniform1i(programView.entity.getUniformID("lightCount"), lights.size());
        glUniform1i(programView.entity.getUniformID("useLighting"), object.isApplyLighting() ? 1 : 0);

        for (int lightID = 0; lightID < lights.size(); lightID++) {
            final var prefix = "lights[" + lightID + "]";
            try (final var _ = lightRenderer.getContext().withType(programView.entity.getUniformID(prefix + ".type"))
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

        if (object.entity.getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.entity.getFacetsBufferID());
            glDrawElements(GL_TRIANGLES, object.entity.getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        if (object.entity.getEdgesCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.entity.getEdgesBufferID());
            glDrawElements(GL_LINES, object.entity.getEdgesCount(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    static final class ObjectRenderContext extends AbstractRenderContext {
        private final FloatBuffer viewProjectionMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private final List<LightView> lights = new ArrayList<>();
        private final Vector4f ambientLight = new Vector4f();

        public ObjectRenderContext withViewProjectionMatrix(final FloatBuffer buffer) {
            this.viewProjectionMatrixBuffer.put(buffer.rewind()).flip();
            return this;
        }

        public ObjectRenderContext withModelMatrix(final Vector3f position, final Vector3f rotation, final Vector3f scale) {
            modelMatrix.identity()
                    .translate(position)
                    .rotateX(rotation.x)
                    .rotateY(rotation.y)
                    .rotateZ(rotation.z)
                    .scale(scale);
            modelMatrix.get(modelMatrixBuffer);

            modelMatrix.normal(normalMatrix);
            normalMatrix.get(normalMatrixBuffer);
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
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
            ambientLight.set(0.f);
            lights.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(viewProjectionMatrixBuffer);
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
