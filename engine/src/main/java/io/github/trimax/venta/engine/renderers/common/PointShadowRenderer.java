package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.*;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.FrustumHelper;
import io.github.trimax.venta.engine.helpers.PointShadowHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.shadow.PointShadowFrame;
import io.github.trimax.venta.engine.model.common.shadow.ShadowMap;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Draws the shadow maps of the point lights before the scene: the few nearest lights that cast shadows get a cube of six
 * faces each (the faces are layers of one map), drawn as the light sees the objects and the terrain (see
 * {@link ShadowCasterRenderer}), only the depth is kept. The shaders of the scene take the maps from {@link #getFrame()}.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointShadowRenderer {
    private final ShadowCasterRenderer casterRenderer;
    private final Memory memory;

    private final PointShadowHelper helper = new PointShadowHelper();

    private ShadowMap map;

    /** The point shadows of the last frame, it is not active when no light has a map */
    @Getter
    private final PointShadowFrame frame = new PointShadowFrame(DefinitionsCommon.MAX_POINT_SHADOWS, DefinitionsCommon.POINT_SHADOW_FACES);

    /** Draws the shadow maps of the frame for the lights of the scene that cast shadows */
    public void render(final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera, final Engine.VentaTime time) {
        frame.clear();
        if (scene == null || scene.getLights().isEmpty())
            return;

        helper.assign(frame, scene.getLights(), camera.getPosition());
        if (!frame.isActive())
            return;

        if (map == null)
            map = new ShadowMap(memory, DefinitionsCommon.POINT_SHADOW_RESOLUTION, DefinitionsCommon.MAX_POINT_SHADOWS * DefinitionsCommon.POINT_SHADOW_FACES);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        for (int slot = 0; slot < frame.getCount(); slot++)
            for (int face = 0; face < frame.getUsedFaces(slot); face++) {
                map.bindLayer(slot * frame.getFaces() + face);
                glClear(GL_DEPTH_BUFFER_BIT);

                // A face is a view of the light with a perspective: everything that is out of it is culled
                casterRenderer.draw(scene, frame.getMatrix(slot, face), FrustumHelper.ALL_PLANES, time);
            }

        glUseProgram(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glActiveTexture(TextureUnit.PointShadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, map.getTextureID());
        glActiveTexture(GL_TEXTURE0);
    }

    /** Releases the shadow map, the next frame with such lights creates it again */
    public void release() {
        frame.clear();

        if (map != null) {
            map.release();
            map = null;
        }
    }
}
