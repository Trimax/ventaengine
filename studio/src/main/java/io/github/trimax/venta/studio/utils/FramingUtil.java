package io.github.trimax.venta.studio.utils;

import java.util.Collection;

import org.joml.Vector3f;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Where to look at a scene from: the middle of what is in it and how big it is.
 */
@UtilityClass
public final class FramingUtil {
    /** A scene that is a point or a few things close to each other is still looked at from this far */
    public static final float MINIMAL_RADIUS = 5.f;

    /**
     * The place that is looked at, and the radius of the sphere that holds the points.
     *
     * @param center the middle of the box of the points
     * @param radius at least {@link #MINIMAL_RADIUS}
     */
    public record Frame(Vector3f center, float radius) {
    }

    /** The frame of the points, null when there are none */
    public Frame frame(@NonNull final Collection<Vector3f> points) {
        if (points.isEmpty())
            return null;

        final var minimum = new Vector3f(Float.MAX_VALUE);
        final var maximum = new Vector3f(-Float.MAX_VALUE);
        for (final var point : points) {
            minimum.min(point);
            maximum.max(point);
        }

        final var center = new Vector3f(minimum).add(maximum).mul(0.5f);
        final var radius = new Vector3f(maximum).sub(minimum).length() * 0.5f;

        return new Frame(center, Math.max(radius, MINIMAL_RADIUS));
    }
}
