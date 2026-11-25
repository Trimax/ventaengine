package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.reflection.NearestSelection;
import io.github.trimax.venta.engine.model.common.reflection.ReflectionProbe;
import io.github.trimax.venta.engine.model.common.reflection.ReflectionProbes;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.renderers.instance.SceneInstanceRenderer;
import io.github.trimax.venta.engine.utils.CubeFaceUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The dynamic reflections of the objects: the objects whose material asks for them (see {@code dynamicReflections} of a
 * material) get a cube map of their surroundings, drawn from their position, and reflect it in place of the skybox.
 *
 * <p>The nearest objects to the camera that are in the range of their material have probes (at most {@link #MAXIMAL_PROBES});
 * the others reflect the skybox. A frame draws one face of one probe, so what it costs doesn't depend on the number of the probes,
 * and a probe is refreshed in six frames after its turn comes. A probe is used when all its faces are drawn.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionProbeRenderer {
    /** How many objects have probes at once */
    public static final int MAXIMAL_PROBES = 4;

    /** The nearest plane of the cameras of the faces: the object itself is not drawn, so it can be close */
    private static final float NEAR = 0.1f;

    private final SceneInstanceRenderer sceneRenderer;
    private final EnvironmentRenderer environmentRenderer;
    private final Memory memory;

    @Getter
    private final ReflectionProbes probes = new ReflectionProbes();

    private final NearestSelection<ObjectInstanceImplementation> selection = new NearestSelection<>(MAXIMAL_PROBES);
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    private final Vector3f position = new Vector3f();
    private long frame;

    /**
     * Chooses the objects that have probes, makes and releases them, and draws a face of one probe. The framebuffer of the
     * window is bound after it, the viewport is the caller's to set.
     */
    public void render(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final WindowState window, final Engine.VentaTime time) {
        frame++;

        // The reflections are of the light of the environment: a scene without a skybox has none
        if (scene == null || scene.getSkybox() == null || environmentRenderer.getEnvironment() == null) {
            probes.clear();
            return;
        }

        select(scene, camera);
        probes.retain(selection.asPredicate());

        ReflectionProbe next = null;
        for (int index = 0; index < selection.size(); index++) {
            final var object = selection.get(index);
            final var settings = object.getMaterial().getReflections();

            var probe = probes.get(object);
            if (probe != null && probe.getSettings().getResolution() != settings.getResolution()) {
                probes.remove(object);
                probe = null;
            }

            if (probe == null) {
                probe = new ReflectionProbe(memory, object, settings);
                probes.put(probe);
            }

            probe.advance((float) time.getDelta());
            if (probe.isDrawing() && (next == null || probe.getLastFrame() < next.getLastFrame()))
                next = probe;
        }

        if (next != null)
            draw(next, scene, camera, window, time);
    }

    /** Releases the cube maps, the next frame with such objects makes them again */
    public void release() {
        probes.clear();
    }

    private void select(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera) {
        selection.clear();

        for (final var object : scene.getObjects()) {
            final var material = object.getMaterial();
            if (!object.isVisible() || material == null || !material.hasReflections())
                continue;

            object.getTransform().getMatrix().getTranslation(position);
            final var distance = position.distanceSquared(camera.getPosition());
            final var range = material.getReflections().getRange();
            if (distance <= range * range)
                selection.offer(object, distance);
        }
    }

    private void draw(final ReflectionProbe probe, final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final WindowState window, final Engine.VentaTime time) {
        final var object = probe.getObject();
        object.getTransform().getMatrix().getTranslation(position);

        final var face = probe.getNextFace();
        CubeFaceUtil.getView(face, position, view);
        CubeFaceUtil.getProjection(NEAR, window.getView().getFar(), projection);

        probe.bindFace(face);
        try (final var _ = sceneRenderer.withContext(null)
                .withTime(time)
                .withReflection(camera, view, projection, position)
                .excluding(object)) {
            sceneRenderer.render(scene);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        probe.finishFace(frame);
    }
}
