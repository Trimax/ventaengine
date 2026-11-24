package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL20C.glUseProgram;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.CameraBinder;
import io.github.trimax.venta.engine.binders.FogBinder;
import io.github.trimax.venta.engine.binders.LightBinder;
import io.github.trimax.venta.engine.binders.MatrixBinder;
import io.github.trimax.venta.engine.binders.ShadowBinder;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.helpers.ScreenPassHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.post.LightShaftTargets;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Adds the shafts of the light of the sun to the resolved scene: the sun is scattered in the exponential fog, and the
 * shadow maps tell where its light reaches. Two passes make them: a march along the view rays into an image of half the
 * size of the scene, and an enlargement of that image, weighted by the lengths of the rays, that is added to the scene.
 *
 * <p>The effect needs the fog that scatters the light (an exponential one), a sun that casts shadows, and the shafts
 * in the scene; a scene without one of them has none.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightShaftRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final ShadowRenderer shadowRenderer;
    private final CameraBinder cameraBinder;
    private final MatrixBinder matrixBinder;
    private final ShadowBinder shadowBinder;
    private final LightBinder lightBinder;
    private final FogBinder fogBinder;
    private final Memory memory;

    private final Matrix4f viewProjection = new Matrix4f();
    private final Matrix4f inverse = new Matrix4f();

    private LightShaftTargets targets;
    private FloatBuffer inverseBuffer;
    private ScreenPassHelper screenPass;

    /** Whether the scene has shafts to draw this frame: the shadows of the frame are the ones that tell where the light reaches */
    public boolean isActive(final SceneInstanceImplementation scene) {
        final var shafts = scene == null ? null : scene.getLightShafts();

        return shafts != null && shafts.getIntensity() > 0.f
                && scene.getFog() != null && scene.getFog().isExponential()
                && scene.getDirectionalLight() != null && shadowRenderer.getFrame().isActive();
    }

    /**
     * Adds the shafts to the resolved scene, the depth of which must be resolved as well.
     *
     * @param scene        the scene the shafts are active in, see {@link #isActive(SceneInstanceImplementation)}
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
            targets = new LightShaftTargets(memory, width, height);
        }

        if (inverseBuffer == null)
            inverseBuffer = MemoryUtil.memAllocFloat(16);

        window.getProjectionMatrix().mul(camera.getViewMatrix(), viewProjection).invert(inverse).get(inverseBuffer);

        try (final var _ = getScreenPass().begin()) {
            march(scene, camera, postRenderer);
            enlarge(scene, camera, postRenderer, width, height);
        } finally {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    /** Deletes the images, the buffer and the vertex array, the next frame with shafts makes them again */
    public void release() {
        releaseTargets();

        if (inverseBuffer != null) {
            MemoryUtil.memFree(inverseBuffer);
            inverseBuffer = null;
        }

        if (screenPass != null)
            screenPass.release();
    }

    /** The march along the view rays: the light of the sun scattered in the fog is written to the image of half the size */
    private void march(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final PostProcessingRenderer postRenderer) {
        final var program = programRegistry.get(ProgramType.LightShafts.getProgramName());
        glUseProgram(program.getInternalID());

        bindScene(program, scene, camera);
        bindDepth(program, postRenderer);
        glUniform1i(program.getUniformID(ShaderUniform.LightShaftsSteps), scene.getLightShafts().getSteps());
        glUniform1f(program.getUniformID(ShaderUniform.LightShaftsAnisotropy), scene.getLightShafts().getAnisotropy());

        getScreenPass().draw(targets.getFramebufferID(), targets.getWidth(), targets.getHeight());
    }

    /** The image is enlarged to the size of the scene and added to it: the scene gets the light, so the bloom takes the shafts too */
    private void enlarge(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final PostProcessingRenderer postRenderer,
                         final int width, final int height) {
        final var program = programRegistry.get(ProgramType.LightShaftsUpsample.getProgramName());
        glUseProgram(program.getInternalID());

        bindScene(program, scene, camera);
        bindDepth(program, postRenderer);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, targets.getTextureID());
        glUniform1i(program.getUniformID(ShaderUniform.TextureShafts), 1);
        glActiveTexture(GL_TEXTURE0);
        glUniform1f(program.getUniformID(ShaderUniform.LightShaftsIntensity), scene.getLightShafts().getIntensity());

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ONE);
        getScreenPass().draw(postRenderer.getResolvedColorFramebufferID(), width, height);
        glDisable(GL_BLEND);
    }

    /** What both passes need to find the points of the rays and the light that reaches them */
    private void bindScene(final ProgramEntityImplementation program,
                           final SceneInstanceImplementation scene, final CameraInstanceImplementation camera) {
        cameraBinder.bind(program, camera);
        matrixBinder.bindViewProjectionInverseMatrix(program, inverseBuffer);
        lightBinder.bind(program, scene.getDirectionalLight());
        fogBinder.bind(program, scene.getFog(), scene.getDirectionalLight());
        shadowBinder.bind(program);
    }

    private static void bindDepth(final ProgramEntityImplementation program,
                                  final PostProcessingRenderer postRenderer) {
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
