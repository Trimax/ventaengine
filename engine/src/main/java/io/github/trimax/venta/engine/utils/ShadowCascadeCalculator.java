package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;
import io.github.trimax.venta.engine.model.common.shadow.ShadowFrame;
import lombok.NonNull;

/**
 * Cascaded shadow maps: the view frustum up to the shadow distance is split along the view direction, and every part
 * gets the orthographic projection of the sun that fits it.
 *
 * <p>The cascades are computed every frame, so a calculator keeps the vectors and the matrices it works with and writes
 * the result into a {@link ShadowFrame}: once they are made, computing allocates nothing. It is not thread safe.
 */
public final class ShadowCascadeCalculator {
    /** 0 splits the distance evenly, 1 logarithmically (the near parts get more detail) */
    private static final float SPLIT_LAMBDA = 0.75f;

    /** The radius of the bounding sphere is rounded up to this step: the map doesn't resize with every small change */
    private static final float RADIUS_STEP = 1.f / 16.f;

    private static final float PARALLEL_LIMIT = 0.99f;

    private static final int CORNERS = 8;

    private final Vector3f[] corners = new Vector3f[CORNERS];
    private final Vector3f direction = new Vector3f();
    private final Vector3f center = new Vector3f();
    private final Vector3f snapped = new Vector3f();
    private final Matrix4f lightView = new Matrix4f();
    private final Matrix4f inverseView = new Matrix4f();

    private float[] splits = new float[4];

    public ShadowCascadeCalculator() {
        for (int i = 0; i < CORNERS; i++)
            corners[i] = new Vector3f();
    }

    /** The distance of the near plane of a perspective projection */
    public static float getNear(@NonNull final Matrix4fc projection) {
        return projection.m32() / (projection.m22() - 1.f);
    }

    /**
     * The distances where the cascades end: a mix of the even and the logarithmic splits of the range from the near
     * plane to the shadow distance. The last one is the shadow distance.
     *
     * @param splits receives the distances of the first {@code count} cascades
     */
    public static void getSplits(final float near, final float distance, final int count, final float @NonNull [] splits) {
        check(near, distance, count);
        if (splits.length < count)
            throw new IllegalArgumentException(String.format("The array has room for %d splits, %d are needed", splits.length, count));

        for (int i = 1; i <= count; i++) {
            final var part = (float) i / count;
            final var logarithmic = near * (float) Math.pow(distance / near, part);
            final var linear = near + (distance - near) * part;
            splits[i - 1] = SPLIT_LAMBDA * logarithmic + (1.f - SPLIT_LAMBDA) * linear;
        }

        splits[count - 1] = distance;
    }

    /**
     * Computes the cascades of a camera into a frame.
     *
     * <p>A cascade is fitted to the bounding sphere of its part of the frustum, so the size of its map doesn't depend on the
     * direction of the camera (the shadows don't shimmer when it turns), and its center is snapped to the texels of
     * the map (the shadows don't swim when the camera moves).
     *
     * @param frame          receives the cascades, it must have room for them
     * @param view           the view matrix of the camera
     * @param projection     the perspective projection of the camera
     * @param lightDirection the direction the sun light goes in
     * @param distance       how far from the camera the shadows are drawn
     * @param count          the number of the cascades
     * @param resolution     the size of the shadow map of a cascade in texels
     */
    public void calculate(@NonNull final ShadowFrame frame,
                          @NonNull final Matrix4fc view,
                          @NonNull final Matrix4fc projection,
                          @NonNull final Vector3fc lightDirection,
                          final float distance,
                          final int count,
                          final int resolution) {
        final var near = getNear(projection);
        check(near, distance, count);
        if (resolution < 1)
            throw new IllegalArgumentException("The shadow map must have a texel, got " + resolution);

        if (lightDirection.lengthSquared() == 0.f)
            throw new IllegalArgumentException("The direction of the light must not be zero");

        frame.begin(count, distance);

        if (splits.length < count)
            splits = new float[count];

        getSplits(near, distance, count, splits);

        direction.set(lightDirection).normalize();
        if (Math.abs(direction.y) > PARALLEL_LIMIT)
            lightView.setLookAt(0.f, 0.f, 0.f, direction.x, direction.y, direction.z, 1.f, 0.f, 0.f);
        else
            lightView.setLookAt(0.f, 0.f, 0.f, direction.x, direction.y, direction.z, 0.f, 1.f, 0.f);

        inverseView.set(view).invert();

        var start = near;
        for (int i = 0; i < count; i++) {
            setCorners(projection, start, splits[i]);
            fit(frame.getCascade(i), splits[i], resolution);
            start = splits[i];
        }
    }

    private void fit(final ShadowCascade cascade, final float far, final int resolution) {
        center.zero();
        for (final var corner : corners)
            center.add(corner);
        center.div(CORNERS);

        var radius = 0.f;
        for (final var corner : corners)
            radius = Math.max(radius, corner.distance(center));

        radius = (float) Math.ceil(radius / RADIUS_STEP) * RADIUS_STEP;

        // The center in the space of the sun is moved to a texel of the map, so the shadows keep to the world
        final var texel = 2.f * radius / resolution;
        lightView.transformPosition(center, snapped);
        snapped.x = (float) Math.floor(snapped.x / texel) * texel;
        snapped.y = (float) Math.floor(snapped.y / texel) * texel;

        // The sun is as far behind the part as its radius: what is nearer to the sun than the near plane is clamped to it
        // when the map is drawn, so it still casts its shadow
        cascade.getMatrix()
                .setOrtho(snapped.x - radius, snapped.x + radius, snapped.y - radius, snapped.y + radius, -snapped.z - 2.f * radius, -snapped.z + radius)
                .mul(lightView);
        cascade.set(far, texel);
    }

    /** The corners of the part of the frustum between two distances, in the world space */
    private void setCorners(final Matrix4fc projection, final float start, final float end) {
        final var halfWidth = 1.f / projection.m00();
        final var halfHeight = 1.f / projection.m11();

        var index = 0;
        for (int near = 0; near < 2; near++) {
            final var depth = near == 0 ? start : end;
            for (int x = -1; x <= 1; x += 2)
                for (int y = -1; y <= 1; y += 2)
                    inverseView.transformPosition(x * halfWidth * depth, y * halfHeight * depth, -depth, corners[index++]);
        }
    }

    private static void check(final float near, final float distance, final int count) {
        if (count < 1)
            throw new IllegalArgumentException("There must be a cascade, got " + count);

        if (near <= 0.f || distance <= near)
            throw new IllegalArgumentException(String.format("The shadow distance %s must be greater than the near plane %s, which must be positive", distance, near));
    }
}
