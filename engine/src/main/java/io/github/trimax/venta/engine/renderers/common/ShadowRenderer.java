package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.FrustumHelper;
import io.github.trimax.venta.engine.helpers.ShadowCascadeHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.shadow.ShadowFrame;
import io.github.trimax.venta.engine.model.common.shadow.ShadowMap;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Draws the shadow maps of the sun before the scene: the objects and the terrain are drawn into the cascades as the
 * sun sees them (see {@link ShadowCasterRenderer}), only the depth is kept. The shaders of the scene take the maps from
 * {@link #getFrame()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRenderer {
    private final ShadowCasterRenderer casterRenderer;
    private final Memory memory;

    private final ShadowCascadeHelper calculator = new ShadowCascadeHelper();
    private final Vector3f cameraForward = new Vector3f();

    private ShadowMap map;

    /** The shadows of the last frame, it is not active when the scene has none */
    @Getter
    private final ShadowFrame frame = new ShadowFrame(DefinitionsCommon.MAX_CASCADES);

    /** Draws the shadow maps of the frame when the sun of the scene casts shadows */
    public void render(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, @NonNull final WindowState window,
                       final Engine.VentaTime time) {
        frame.deactivate();

        final var light = scene == null ? null : scene.getDirectionalLight();
        if (light == null || !light.isCastShadows())
            return;

        if (map == null)
            map = new ShadowMap(memory, DefinitionsCommon.SHADOW_RESOLUTION, DefinitionsCommon.MAX_CASCADES);

        final var view = camera.getViewMatrix();
        calculator.calculate(frame, view, window.getProjectionMatrix(), light.getDirection(), light.getShadowDistance(),
                DefinitionsCommon.MAX_CASCADES, DefinitionsCommon.SHADOW_RESOLUTION);

        // What is nearer to the sun than the cascade is clamped to its near plane and still casts its shadow
        glEnable(GL_DEPTH_CLAMP);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        for (int i = 0; i < frame.getCount(); i++) {
            map.bindLayer(i);
            glClear(GL_DEPTH_BUFFER_BIT);

            // A cascade holds what the sun sees in its box: the objects beside the box can't shadow anything in it, and only
            // the sides of the box cull, because what is nearer to the sun than the map still casts its shadow into it
            casterRenderer.draw(scene, frame.getCascade(i).getMatrix(), FrustumHelper.SIDE_PLANES, time);
        }

        glUseProgram(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_CLAMP);

        glActiveTexture(TextureUnit.Shadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, map.getTextureID());
        glActiveTexture(GL_TEXTURE0);

        cameraForward.set(-view.m02(), -view.m12(), -view.m22()).normalize();
        frame.activate(cameraForward);
    }

    /** Releases the shadow map, the next frame with shadows creates it again */
    public void release() {
        frame.deactivate();

        if (map != null) {
            map.release();
            map = null;
        }
    }
}
