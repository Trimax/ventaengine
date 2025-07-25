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
import com.venta.engine.enums.ShaderLightUniform;
import com.venta.engine.enums.ShaderUniform;
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
final class ObjectRenderer extends AbstractRenderer<ObjectManager.ObjectEntity, ObjectView, ObjectRenderer.ObjectRenderContext, SceneRenderer.SceneRenderContext> {
    private final MaterialRenderer materialRenderer;
    private final LightRenderer lightRenderer;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

    @Override
    @SneakyThrows
    public void render(final ObjectView object) {
        if (!object.isVisible())
            return;

        final var programView = object.getProgram();
        if (programView == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(programView.entity.getIdAsInteger());
        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        glUniform3f(programView.entity.getUniformID(ShaderUniform.CameraPosition), context.getParent().getCameraPosition().x(),
                context.getParent().getCameraPosition().y(), context.getParent().getCameraPosition().z());

        glUniform4f(programView.entity.getUniformID(ShaderUniform.AmbientLight), context.getAmbientLight().x(),
                context.getAmbientLight().y(), context.getAmbientLight().z(), context.getAmbientLight().w());

        glUniformMatrix4fv(programView.entity.getUniformID(ShaderUniform.MatrixViewProjection), false,
                context.getParent().getViewProjectionMatrixBuffer());
        glUniformMatrix3fv(programView.entity.getUniformID(ShaderUniform.MatrixNormal), false, context.getNormalMatrixBuffer());
        glUniformMatrix4fv(programView.entity.getUniformID(ShaderUniform.MatrixModel), false, context.getModelMatrixBuffer());

        try (final var _ = materialRenderer.withContext(getContext())
                .withTextureDiffuse(programView.entity.getUniformID(ShaderUniform.TextureDiffuse),
                        programView.entity.getUniformID(ShaderUniform.UseTextureDiffuseFlag))
                .withTextureHeight(programView.entity.getUniformID(ShaderUniform.TextureHeight),
                        programView.entity.getUniformID(ShaderUniform.UseTextureHeight))
                .withTextureNormal(programView.entity.getUniformID(ShaderUniform.TextureNormal),
                        programView.entity.getUniformID(ShaderUniform.UseTextureNormal))
                .withTextureAmbientOcclusion(programView.entity.getUniformID(ShaderUniform.TextureAmbientOcclusion),
                        programView.entity.getUniformID(ShaderUniform.UseTextureAmbientOcclusion))
                .withRoughness(programView.entity.getUniformID(ShaderUniform.TextureRoughness),
                        programView.entity.getUniformID(ShaderUniform.UseTextureRoughness))) {
            materialRenderer.render(object.getMaterial());
        }

        final var lights = context.getLights();
        glUniform1i(programView.entity.getUniformID(ShaderUniform.LightCount), lights.size());
        glUniform1i(programView.entity.getUniformID(ShaderUniform.UseLighting), object.isApplyLighting() ? 1 : 0);

        for (int lightID = 0; lightID < lights.size(); lightID++) {
            try (final var _ = lightRenderer.withContext(getContext())
                    .withType(programView.entity.getUniformID(ShaderLightUniform.Type.getUniformName(lightID)))
                    .withColor(programView.entity.getUniformID(ShaderLightUniform.Color.getUniformName(lightID)))
                    .withEnabled(programView.entity.getUniformID(ShaderLightUniform.Enabled.getUniformName(lightID)))
                    .withPosition(programView.entity.getUniformID(ShaderLightUniform.Position.getUniformName(lightID)))
                    .withDirection(programView.entity.getUniformID(ShaderLightUniform.Direction.getUniformName(lightID)))
                    .withIntensity(programView.entity.getUniformID(ShaderLightUniform.Intensity.getUniformName(lightID)))
                    .withCastShadows(programView.entity.getUniformID(ShaderLightUniform.CastShadows.getUniformName(lightID)))
                    .withAttenuationLinear(programView.entity.getUniformID(ShaderLightUniform.AttenuationLinear.getUniformName(lightID)))
                    .withAttenuationConstant(programView.entity.getUniformID(ShaderLightUniform.AttenuationConstant.getUniformName(lightID)))
                    .withAttenuationQuadratic(programView.entity.getUniformID(ShaderLightUniform.AttenuationQuadratic.getUniformName(lightID)))) {
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
    static final class ObjectRenderContext extends AbstractRenderContext<SceneRenderer.SceneRenderContext> {
        private final FloatBuffer modelMatrixBuffer = MemoryUtil.memAllocFloat(16);
        private final FloatBuffer normalMatrixBuffer = MemoryUtil.memAllocFloat(9);
        private final Matrix3f normalMatrix = new Matrix3f();
        private final Matrix4f modelMatrix = new Matrix4f();

        private final List<LightView> lights = new ArrayList<>();
        private final Vector4f ambientLight = new Vector4f();

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
            normalMatrixBuffer.clear();
            modelMatrixBuffer.clear();
            ambientLight.set(0.f);
            lights.clear();
        }

        @Override
        public void destroy() {
            MemoryUtil.memFree(normalMatrixBuffer);
            MemoryUtil.memFree(modelMatrixBuffer);
        }
    }
}
