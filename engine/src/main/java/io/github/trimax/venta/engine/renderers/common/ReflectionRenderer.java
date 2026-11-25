package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.helpers.WaterReflectionHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.post.ReflectionTarget;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Prepares the reflection of the water: the nearest water that has reflections gets the scene drawn once more as its mirror
 * image sees it, into a texture of half the size of the scene (the engine renderer draws it, see {@link #begin}). The water reads
 * the texture by the projection of the mirror image, see {@link #getTextureID()} and {@link #getViewProjectionMatrixBuffer()}.
 *
 * <p>Only one water is reflected in a frame, the others reflect the sky. A camera under the water has no reflection.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionRenderer {
    private final Memory memory;

    private final WaterReflectionHelper helper = new WaterReflectionHelper();
    private final Vector3f waterPosition = new Vector3f();

    private ReflectionTarget target;
    private FloatBuffer matrixBuffer;

    /** The water that has its reflection drawn in this frame, null when there is none */
    @Getter
    private WaterSurfaceInstanceImplementation water;

    /** Whether a reflection is drawn in this frame */
    public boolean isActive() {
        return water != null;
    }

    /**
     * The texture of the reflection: valid when {@link #isActive()}.
     */
    public int getTextureID() {
        return target == null ? 0 : target.getTextureID();
    }

    /**
     * The view and projection matrices of the mirror image of the camera: the projection holds the plane of the water as its
     * near plane.
     */
    public Matrix4fc getViewMatrix() {
        return helper.getViewMatrix();
    }

    public Matrix4fc getProjectionMatrix() {
        return helper.getProjectionMatrix();
    }

    /** Where the mirror image of the camera is */
    public Vector3fc getCameraPosition() {
        return helper.getPosition();
    }

    /**
     * The view projection matrix of the mirror image as the shaders take it: the water finds the point it shows in the
     * reflection by it.
     */
    public FloatBuffer getViewProjectionMatrixBuffer() {
        return matrixBuffer;
    }

    /**
     * Chooses the water that is reflected and binds the framebuffer it is drawn into, cleared; the matrices of the mirror image
     * of the camera are made. When it returns true, the caller draws the scene into the framebuffer and calls {@link #end()}.
     *
     * @return whether a reflection is to be drawn
     */
    public boolean begin(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, @NonNull final WindowState window) {
        water = choose(scene, camera);
        if (water == null)
            return false;

        try (final var stack = stackPush()) {
            final var width = stack.mallocInt(1);
            final var height = stack.mallocInt(1);
            glfwGetFramebufferSize(window.getInternalID(), width, height);

            // A minimized window has no pixels
            if (width.get(0) < 1 || height.get(0) < 1) {
                water = null;
                return false;
            }

            if (target == null || target.getSceneWidth() != width.get(0) || target.getSceneHeight() != height.get(0)) {
                releaseTarget();
                target = new ReflectionTarget(memory, width.get(0), height.get(0));
            }
        }

        water.getTransform().getMatrix().getTranslation(waterPosition);
        helper.update(camera.getPosition(), camera.getFront(), camera.getUp(), window.getProjectionMatrix(), waterPosition.y());

        if (matrixBuffer == null)
            matrixBuffer = MemoryUtil.memAllocFloat(16);

        helper.getViewProjectionMatrix().get(matrixBuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, target.getFramebufferID());
        glViewport(0, 0, target.getWidth(), target.getHeight());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        return true;
    }

    /** Goes back to the window: the scene draws into its own framebuffer after it */
    public void end() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /** Releases the image and the buffer of the matrix, the next frame with such water makes them again */
    public void release() {
        water = null;
        releaseTarget();

        if (matrixBuffer != null) {
            MemoryUtil.memFree(matrixBuffer);
            matrixBuffer = null;
        }
    }

    /** The nearest of the visible waters that have reflections, if the camera is over its plane */
    private WaterSurfaceInstanceImplementation choose(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera) {
        if (scene == null)
            return null;

        WaterSurfaceInstanceImplementation chosen = null;
        var nearest = Float.MAX_VALUE;
        for (final var surface : scene.getWaterSurfaces()) {
            if (!surface.isVisible() || !surface.getMaterial().isReflections())
                continue;

            surface.getTransform().getMatrix().getTranslation(waterPosition);
            if (camera.getPosition().y() <= waterPosition.y())
                continue;

            final var distance = waterPosition.distanceSquared(camera.getPosition());
            if (distance < nearest) {
                nearest = distance;
                chosen = surface;
            }
        }

        return chosen;
    }

    private void releaseTarget() {
        if (target != null) {
            target.release();
            target = null;
        }
    }
}
