package io.github.trimax.venta.engine.helpers;

import java.util.Collection;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.utils.LodUtil;
import lombok.NonNull;

/**
 * Chooses the level of detail of the objects of a scene for a camera: the smaller an object is on the screen, the simpler
 * the model that is drawn. A change of the level is not a jump: the new level fades in while the old one fades out
 * (the renderers dither the two).
 *
 * <p>It is made for every frame, so one instance keeps its vectors and a frame allocates nothing.
 */
public final class LodHelper {
    /** The share of a border between two levels that the size must pass to change the level, so the level doesn't flicker */
    public static final float HYSTERESIS = 0.1f;

    /** The time in seconds in which a level fades into another */
    public static final float FADE_DURATION = 0.35f;

    private final Vector3f center = new Vector3f();
    private final Vector3f scale = new Vector3f();

    /**
     * Moves the objects on to the levels they need now.
     *
     * @param objects         the objects of the scene: the ones without simpler models or hidden are skipped
     * @param camera          the position of the camera
     * @param projectionScale the focal length of the projection of the camera, see {@link LodUtil#screenSize}
     * @param delta           the seconds since the last frame
     */
    public void update(@NonNull final Collection<ObjectInstanceImplementation> objects, @NonNull final Vector3fc camera,
                       final float projectionScale, final float delta) {
        for (final var object : objects) {
            if (!object.hasLods() || !object.isVisible())
                continue;

            object.advanceLod(delta, FADE_DURATION);

            // The box is the one of the main model, in the space of the object: its sphere and the matrix give the sphere in the world
            final var box = object.getBox();
            final var matrix = object.getTransform().getMatrix();
            final var radius = 0.5f * box.size().length() * maxScale(matrix.getScale(scale));
            final var size = LodUtil.screenSize(radius, matrix.transformPosition(box.center(), center).distance(camera), projectionScale);

            object.setLod(LodUtil.select(object.getLodSizes(), object.getLod(), size, HYSTERESIS));
        }
    }

    private static float maxScale(final Vector3fc scale) {
        return Math.max(scale.x(), Math.max(scale.y(), scale.z()));
    }
}
