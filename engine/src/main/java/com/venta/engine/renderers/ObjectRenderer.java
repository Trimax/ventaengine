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
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.SceneView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer extends AbstractRenderer<ObjectView, ObjectRenderer.ObjectRenderContext, SceneRenderer.SceneRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ObjectManager.ObjectAccessor objectAccessor;
    private final MaterialRenderer materialRenderer;
    private final LightRenderer lightRenderer;

    @Override
    protected ObjectRenderContext createContext() {
        return new ObjectRenderContext();
    }

    @Override
    public void render(final ObjectView object) {
        render(objectAccessor.get(object.getID()));
    }

    private void render(final ObjectManager.ObjectEntity object) {
        if (!object.isVisible())
            return;

        final var programView = object.getProgram();
        if (programView == null)
            return;

        final var program = programAccessor.get(programView.getID());

        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        glUseProgram(program.getInternalID());
        glBindVertexArray(object.getVertexArrayObjectID());
        glPolygonMode(GL_FRONT_AND_BACK, object.getDrawMode().getMode());

        glUniform3f(program.getUniformID(ShaderUniform.CameraPosition), context.getParent().getCameraPosition().x(),
                context.getParent().getCameraPosition().y(), context.getParent().getCameraPosition().z());

        glUniform4f(program.getUniformID(ShaderUniform.AmbientLight), context.getAmbientLight().x(),
                context.getAmbientLight().y(), context.getAmbientLight().z(), context.getAmbientLight().w());

        glUniformMatrix4fv(programView.getUniformID(ShaderUniform.MatrixViewProjection), false,
                context.getParent().getViewProjectionMatrixBuffer());
        glUniformMatrix3fv(program.getUniformID(ShaderUniform.MatrixNormal), false, context.getNormalMatrixBuffer());
        glUniformMatrix4fv(programView.getUniformID(ShaderUniform.MatrixModel), false, context.getModelMatrixBuffer());

        try (final var _ = materialRenderer.withContext(getContext())
                .withTextureDiffuse(program.getUniformID(ShaderUniform.TextureDiffuse),
                        program.getUniformID(ShaderUniform.UseTextureDiffuseFlag))
                .withTextureHeight(program.getUniformID(ShaderUniform.TextureHeight),
                        program.getUniformID(ShaderUniform.UseTextureHeight))
                .withTextureNormal(program.getUniformID(ShaderUniform.TextureNormal),
                        program.getUniformID(ShaderUniform.UseTextureNormal))
                .withTextureAmbientOcclusion(program.getUniformID(ShaderUniform.TextureAmbientOcclusion),
                        program.getUniformID(ShaderUniform.UseTextureAmbientOcclusion))
                .withRoughness(program.getUniformID(ShaderUniform.TextureRoughness),
                        program.getUniformID(ShaderUniform.UseTextureRoughness))) {
            materialRenderer.render(object.getMaterial());
        }

        final var lights = context.getLights();
        glUniform1i(program.getUniformID(ShaderUniform.LightCount), lights.size());
        glUniform1i(program.getUniformID(ShaderUniform.UseLighting), object.isApplyLighting() ? 1 : 0);

        for (int lightID = 0; lightID < lights.size(); lightID++) {
            try (final var _ = lightRenderer.withContext(getContext())
                    .withType(program.getUniformID(ShaderLightUniform.Type.getUniformName(lightID)))
                    .withColor(program.getUniformID(ShaderLightUniform.Color.getUniformName(lightID)))
                    .withEnabled(program.getUniformID(ShaderLightUniform.Enabled.getUniformName(lightID)))
                    .withPosition(program.getUniformID(ShaderLightUniform.Position.getUniformName(lightID)))
                    .withDirection(program.getUniformID(ShaderLightUniform.Direction.getUniformName(lightID)))
                    .withIntensity(program.getUniformID(ShaderLightUniform.Intensity.getUniformName(lightID)))
                    .withCastShadows(program.getUniformID(ShaderLightUniform.CastShadows.getUniformName(lightID)))
                    .withAttenuationLinear(program.getUniformID(ShaderLightUniform.AttenuationLinear.getUniformName(lightID)))
                    .withAttenuationConstant(program.getUniformID(ShaderLightUniform.AttenuationConstant.getUniformName(lightID)))
                    .withAttenuationQuadratic(program.getUniformID(ShaderLightUniform.AttenuationQuadratic.getUniformName(lightID)))) {
                lightRenderer.render(lights.get(lightID));
            }
        }

        if (object.getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getFacetsBufferID());
            glDrawElements(GL_TRIANGLES, object.getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        if (object.getEdgesCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getEdgesBufferID());
            glDrawElements(GL_LINES, object.getEdgesCount(), GL_UNSIGNED_INT, 0);
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
