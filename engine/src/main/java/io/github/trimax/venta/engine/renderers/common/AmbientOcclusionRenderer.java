package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL20C.glUniform2f;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.helpers.ScreenPassHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.post.AmbientOcclusionTargets;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Darkens the corners and the contacts of the resolved scene: what stands near a surface keeps the light of the sky from
 * it. The occlusion is estimated from the depth of the scene in an image of half the size of the scene, blurred along the
 * two axes (a sample counts less the more its distance from the eye differs from the one of the pixel, so the shade
 * doesn't leak over the edges), and the scene is multiplied by it at its full size.
 *
 * <p>It multiplies the light the scene has, the direct light as well, because the forward shaders give one color: it is
 * a screen space effect of the finished image. A scene without the ambient occlusion has none.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmbientOcclusionRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final Memory memory;

    private final Matrix4f viewProjection = new Matrix4f();
    private final Matrix4f inverse = new Matrix4f();

    private AmbientOcclusionTargets targets;
    private FloatBuffer inverseBuffer;
    private ScreenPassHelper screenPass;

    /** Whether the scene darkens its corners */
    public boolean isActive(final SceneInstanceImplementation scene) {
        final var occlusion = scene == null ? null : scene.getAmbientOcclusion();

        return occlusion != null && occlusion.getIntensity() > 0.f;
    }

    /**
     * Multiplies the resolved scene by the occlusion, the depth of the scene must be resolved as well.
     *
     * @param scene        the scene the occlusion is active in, see {@link #isActive(SceneInstanceImplementation)}
     * @param postRenderer the renderer of the resolved scene
     */
    public void render(@NonNull final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera,
                       @NonNull final WindowState window, @NonNull final PostProcessingRenderer postRenderer) {
        final var width = postRenderer.getWidth();
        final var height = postRenderer.getHeight();
        if (width < 1 || height < 1)
            return;

        if (targets == null || targets.getSceneWidth() != width || targets.getSceneHeight() != height) {
            releaseTargets();
            targets = new AmbientOcclusionTargets(memory, width, height);
        }

        if (inverseBuffer == null)
            inverseBuffer = MemoryUtil.memAllocFloat(16);

        window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjection).invert(inverse).get(inverseBuffer);

        // The size of a unit of the world at the distance of 1 in the pixels of the scene
        final var projectionScale = 0.5f * height * window.getProjectionMatrix().m11();

        try (final var _ = getScreenPass().begin()) {
            estimate(scene, camera, postRenderer, projectionScale);
            blur();
            apply(scene, camera, postRenderer, width, height);
        } finally {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    /** Deletes the images and the buffer, the next frame with the occlusion makes them again */
    public void release() {
        releaseTargets();

        if (inverseBuffer != null) {
            MemoryUtil.memFree(inverseBuffer);
            inverseBuffer = null;
        }

        if (screenPass != null)
            screenPass.release();
    }

    /** The estimate from the depth of the scene is drawn into the first image */
    private void estimate(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera,
                          final PostProcessingRenderer postRenderer, final float projectionScale) {
        final var program = programRegistry.get(ProgramType.AmbientOcclusion.getProgramName());
        glUseProgram(program.getInternalID());

        bindScene(program, camera, postRenderer);
        glUniform1f(program.getUniformID(ShaderUniform.AoRadius), scene.getAmbientOcclusion().getRadius());
        glUniform1f(program.getUniformID(ShaderUniform.AoPower), scene.getAmbientOcclusion().getPower());
        glUniform1f(program.getUniformID(ShaderUniform.AoProjectionScale), projectionScale);

        getScreenPass().draw(targets.getFramebufferID(0), targets.getWidth(), targets.getHeight());
    }

    /** The image is blurred along x into the second one and along y back into the first one */
    private void blur() {
        final var program = programRegistry.get(ProgramType.AmbientOcclusionBlur.getProgramName());
        glUseProgram(program.getInternalID());
        glUniform1i(program.getUniformID(ShaderUniform.TextureAo), 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, targets.getTextureID(0));
        glUniform2f(program.getUniformID(ShaderUniform.AoBlurStep), 1.f / targets.getWidth(), 0.f);
        getScreenPass().draw(targets.getFramebufferID(1), targets.getWidth(), targets.getHeight());

        glBindTexture(GL_TEXTURE_2D, targets.getTextureID(1));
        glUniform2f(program.getUniformID(ShaderUniform.AoBlurStep), 0.f, 1.f / targets.getHeight());
        getScreenPass().draw(targets.getFramebufferID(0), targets.getWidth(), targets.getHeight());
    }

    /** The scene is multiplied by the image, enlarged to its size: the blending takes the color of the scene and the one of the pass and multiplies them */
    private void apply(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final PostProcessingRenderer postRenderer,
                       final int width, final int height) {
        final var program = programRegistry.get(ProgramType.AmbientOcclusionApply.getProgramName());
        glUseProgram(program.getInternalID());

        bindScene(program, camera, postRenderer);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, targets.getTextureID(0));
        glUniform1i(program.getUniformID(ShaderUniform.TextureAo), 1);
        glActiveTexture(GL_TEXTURE0);
        glUniform1f(program.getUniformID(ShaderUniform.AoIntensity), scene.getAmbientOcclusion().getIntensity());

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_DST_COLOR, GL_ZERO, GL_ZERO, GL_ONE);
        getScreenPass().draw(postRenderer.getResolvedColorFramebufferID(), width, height);
        glDisable(GL_BLEND);
    }

    /** What the passes need to find the points of the world the pixels see */
    private void bindScene(final ProgramEntityImplementation program, final CameraInstanceImplementation camera, final PostProcessingRenderer postRenderer) {
        cameraBinder.bind(program, camera);
        matrixBinder.bindViewProjectionInverseMatrix(program, inverseBuffer);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, postRenderer.getResolvedDepthTextureID());
        glUniform1i(program.getUniformID(ShaderUniform.TextureDepth), 0);
    }

    private ScreenPassHelper getScreenPass() {
        if (screenPass == null)
            screenPass = new ScreenPassHelper(memory);

        return screenPass;
    }

    private void releaseTargets() {
        if (targets != null) {
            targets.release();
            targets = null;
        }
    }
}
