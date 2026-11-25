package io.github.trimax.venta.engine.helpers;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shadow.PointShadowFrame;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import lombok.NonNull;

/**
 * Chooses the point lights that get a shadow map in a frame and makes the matrices of the faces of their cubes. The maps
 * are costly (six passes over the scene for every light), so only the few nearest lights that cast shadows have them.
 *
 * <p>The six faces look along the axes, in the order +x, -x, +y, -y, +z, -z: the shaders choose the face of a point by the
 * axis that it lies the most along, and this order is theirs too. Every face sees a bit more than 90 degrees, so the
 * filter of a shadow at the edge of a face still reads the face and not what is beyond it.
 *
 * <p>It is used in every frame, so one instance keeps its scratch objects and a frame allocates nothing.
 */
public final class PointShadowHelper {
    /** The directions the faces look along */
    public static final Vector3fc[] FACE_DIRECTIONS = {
            new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};

    /** The up directions of the faces: not along the direction, or the face could not be looked at */
    public static final Vector3fc[] FACE_UPS = {
            new Vector3f(0, 1, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, 1), new Vector3f(0, 1, 0), new Vector3f(0, 1, 0)};

    /**
     * The tangent of the half of the angle that a face sees: 1 is 90 degrees, which touches the faces. The margin is a few
     * texels of the map (the filter of a shadow reads about two beyond the point), so the faces overlap by them.
     */
    public static final float FACE_TANGENT = 1.f + 4.f / DefinitionsCommon.POINT_SHADOW_RESOLUTION;

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();

    /** The nearest lights so far, in the order of their distance: the room for the ones that get a map */
    private final LightInstanceImplementation[] nearest = new LightInstanceImplementation[DefinitionsCommon.MAX_POINT_SHADOWS];
    private final float[] distances = new float[DefinitionsCommon.MAX_POINT_SHADOWS];

    /**
     * Fills the frame: the nearest lights to the camera that are on and cast shadows get a slot each, up to its capacity.
     *
     * @param lights the lights of the scene
     * @param camera the position of the camera
     */
    public void assign(@NonNull final PointShadowFrame frame, @NonNull final Collection<LightInstanceImplementation> lights, @NonNull final Vector3fc camera) {
        frame.clear();

        final var capacity = Math.min(frame.getCapacity(), nearest.length);
        var found = 0;
        for (final var light : lights) {
            if (!light.isEnabled() || !light.isCastShadows() || light.getIntensity() <= 0.f)
                continue;

            found = insert(light, light.getPosition().distanceSquared(camera), found, capacity);
        }

        for (int i = 0; i < found; i++) {
            final var light = nearest[i];
            nearest[i] = null;

            final var range = getRange(light);
            fill(frame, frame.add(light, range), light.getPosition(), range);
        }
    }

    /** How far the shadows of a light reach: its range, or the default one for a light that has no range */
    public static float getRange(@NonNull final LightInstanceImplementation light) {
        return light.getRange() > 0.f ? light.getRange() : DefinitionsCommon.DEFAULT_POINT_SHADOW_RANGE;
    }

    /** Puts a light among the nearest ones, the farthest of them drops out when there is no room; returns how many there are now */
    private int insert(final LightInstanceImplementation light, final float distance, final int found, final int capacity) {
        var position = found;
        while (position > 0 && distances[position - 1] > distance)
            position--;

        if (position >= capacity)
            return found;

        final var count = Math.min(found + 1, capacity);
        for (int i = count - 1; i > position; i--) {
            nearest[i] = nearest[i - 1];
            distances[i] = distances[i - 1];
        }

        nearest[position] = light;
        distances[position] = distance;

        return count;
    }

    /** Makes the matrices of the faces of a slot: the view from the light along the direction of the face and the projection */
    private void fill(final PointShadowFrame frame, final int slot, final Vector3fc position, final float range) {
        final var half = (float) Math.atan(FACE_TANGENT);
        projection.setPerspective(2.f * half, 1.f, DefinitionsCommon.POINT_SHADOW_NEAR, range);

        for (int face = 0; face < frame.getFaces(); face++) {
            final var direction = FACE_DIRECTIONS[face];
            final var up = FACE_UPS[face];

            view.setLookAt(position.x(), position.y(), position.z(),
                    position.x() + direction.x(), position.y() + direction.y(), position.z() + direction.z(),
                    up.x(), up.y(), up.z());

            projection.mul(view, frame.getMatrix(slot, face));
        }
    }
}
