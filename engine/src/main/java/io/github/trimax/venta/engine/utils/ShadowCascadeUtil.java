package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.shadow.ShadowCascade;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Cascaded shadow maps: the view frustum up to the shadow distance is split along the view direction, and every part
 * gets the orthographic projection of the sun that fits it.
 */
@UtilityClass
public final class ShadowCascadeUtil {
    /** 0 splits the distance evenly, 1 logarithmically (the near parts get more detail) */
    private static final float SPLIT_LAMBDA = 0.75f;

    /** The radius of the bounding sphere is rounded up to this step: the map doesn't resize with every small change */
    private static final float RADIUS_STEP = 1.f / 16.f;

    private static final float PARALLEL_LIMIT = 0.99f;

    /**
     * The distances where the cascades end: a mix of the even and the logarithmic splits of the range from the near
     * plane to the shadow distance. The last one is the shadow distance.
     */
    public float[] getSplits(final float near, final float distance, final int count) {
        check(near, distance, count);

        final var splits = new float[count];
        for (int i = 1; i <= count; i++) {
            final var part = (float) i / count;
            final var logarithmic = near * (float) Math.pow(distance / near, part);
            final var linear = near + (distance - near) * part;
            splits[i - 1] = SPLIT_LAMBDA * logarithmic + (1.f - SPLIT_LAMBDA) * linear;
        }

        splits[count - 1] = distance;
        return splits;
    }

    /**
     * The cascades of a camera.
     *
     * <p>A cascade is fitted to the bounding sphere of its part of the frustum, so the size of its map doesn't depend on the
     * direction of the camera (the shadows don't shimmer when it turns), and its center is snapped to the texels of
     * the map (the shadows don't swim when the camera moves).
     *
     * @param view           the view matrix of the camera
     * @param projection     the perspective projection of the camera
     * @param lightDirection the direction the sun light goes in
     * @param distance       how far from the camera the shadows are drawn
     * @param count          the number of the cascades
     * @param resolution     the size of the shadow map of a cascade in texels
     */
    public List<ShadowCascade> getCascades(@NonNull final Matrix4fc view,
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

        final var direction = new Vector3f(lightDirection).normalize();
        final var lightView = new Matrix4f().lookAt(new Vector3f(), direction,
                Math.abs(direction.y) > PARALLEL_LIMIT ? new Vector3f(1.f, 0.f, 0.f) : new Vector3f(0.f, 1.f, 0.f));
        final var inverseView = new Matrix4f(view).invert();
        final var splits = getSplits(near, distance, count);

        final List<ShadowCascade> cascades = new ArrayList<>(count);
        var start = near;
        for (final var end : splits) {
            cascades.add(fit(getCorners(inverseView, projection, start, end), lightView, end, resolution));
            start = end;
        }

        return cascades;
    }

    /** The distance of the near plane of a perspective projection */
    public float getNear(@NonNull final Matrix4fc projection) {
        return projection.m32() / (projection.m22() - 1.f);
    }

    private ShadowCascade fit(final Vector3f[] corners, final Matrix4f lightView, final float far, final int resolution) {
        final var center = new Vector3f();
        for (final var corner : corners)
            center.add(corner);
        center.div(corners.length);

        var radius = 0.f;
        for (final var corner : corners)
            radius = Math.max(radius, corner.distance(center));

        radius = (float) Math.ceil(radius / RADIUS_STEP) * RADIUS_STEP;

        // The center in the space of the sun is moved to a texel of the map, so the shadows keep to the world
        final var texel = 2.f * radius / resolution;
        final var snapped = lightView.transformPosition(center, new Vector3f());
        snapped.x = (float) Math.floor(snapped.x / texel) * texel;
        snapped.y = (float) Math.floor(snapped.y / texel) * texel;

        // The sun is as far behind the part as its radius: what is nearer to the sun than the near plane is clamped to it
        // when the map is drawn, so it still casts its shadow
        final var matrix = new Matrix4f().ortho(snapped.x - radius, snapped.x + radius, snapped.y - radius, snapped.y + radius,
                -snapped.z - 2.f * radius, -snapped.z + radius).mul(lightView);

        return new ShadowCascade(matrix, far, texel);
    }

    /** The corners of the part of the frustum between two distances, in the world space */
    private Vector3f[] getCorners(final Matrix4f inverseView, final Matrix4fc projection, final float start, final float end) {
        final var halfWidth = 1.f / projection.m00();
        final var halfHeight = 1.f / projection.m11();

        final var corners = new Vector3f[8];
        var index = 0;
        for (final var depth : new float[]{start, end})
            for (final var x : new float[]{-1.f, 1.f})
                for (final var y : new float[]{-1.f, 1.f})
                    corners[index++] = inverseView.transformPosition(new Vector3f(x * halfWidth * depth, y * halfHeight * depth, -depth));

        return corners;
    }

    private void check(final float near, final float distance, final int count) {
        if (count < 1)
            throw new IllegalArgumentException("There must be a cascade, got " + count);

        if (near <= 0.f || distance <= near)
            throw new IllegalArgumentException(String.format("The shadow distance %s must be greater than the near plane %s, which must be positive", distance, near));
    }
}
