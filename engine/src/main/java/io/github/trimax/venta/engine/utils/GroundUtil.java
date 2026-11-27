package io.github.trimax.venta.engine.utils;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Ray;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.TerrainSurfaceInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.WaterSurfaceInstanceImplementation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Where a ray meets the ground and the water of a scene: the ground is found by walking along the ray until it goes under the surface
 * and narrowing the step down, the water is a plane.
 */
@UtilityClass
public final class GroundUtil {
    /**
     * How far a ray is followed (meters).
     */
    public static final float MAXIMAL_DISTANCE = 5000.f;

    /** The first step of the walk, and how much of the distance walked every step is: far from the camera the steps are longer */
    private static final float MINIMAL_STEP = 0.25f;
    private static final float STEP_PER_DISTANCE = 0.01f;
    private static final int BISECTIONS = 24;

    /** How thick the box of the water is (meters), so it can be seen and chosen from the side */
    private static final float WATER_THICKNESS = 0.2f;

    /**
     * Where a ray meets a surface that is the height over every point of the plane, inside of an area.
     *
     * @param height   the height of the surface at the x and z of the world
     * @param inside   whether the point of the world is over the area that the surface covers
     * @param distance how far the ray is followed
     * @return the distance along the ray to the first point where it is under the surface; {@link RayUtil#MISS} when it never is
     */
    public float intersect(@NonNull final Ray ray, @NonNull final BiFunction<Float, Float, Float> height, @NonNull final Predicate<Vector3fc> inside, final float distance) {
        var previous = 0.f;
        var previousAbove = false;
        var previousInside = false;

        var current = 0.f;
        while (current <= distance) {
            final var point = new Vector3f(ray.origin()).fma(current, ray.direction());
            final var isInside = inside.test(point);
            final var above = point.y > height.apply(point.x, point.z);

            if (isInside && !above) {
                // The ray went under the surface, or came into the area under it (from the side): either way the surface is here
                return previousInside && previousAbove ? narrow(ray, height, previous, current) : current;
            }

            previous = current;
            previousAbove = isInside && above;
            previousInside = isInside;
            current += Math.max(MINIMAL_STEP, current * STEP_PER_DISTANCE);
        }

        return RayUtil.MISS;
    }

    /**
     * Where a ray meets the ground of a terrain, {@link RayUtil#MISS} when it doesn't.
     */
    public float terrain(@NonNull final Ray ray, @NonNull final TerrainSurfaceInstanceImplementation terrain, final float distance) {
        final var inverse = terrain.getTransform().getMatrix().invertAffine(new Matrix4f());
        final var halfX = terrain.getGridMesh().getSize().x() * 0.5f;
        final var halfZ = terrain.getGridMesh().getSize().y() * 0.5f;

        return intersect(ray, terrain::getHeight, point -> {
            final var local = inverse.transformPosition(new Vector3f(point));
            return Math.abs(local.x) <= halfX && Math.abs(local.z) <= halfZ;
        }, distance);
    }

    /**
     * Where a ray meets the ground of the scene: the nearest point of its terrains, {@link RayUtil#MISS} when it meets none.
     */
    public float ground(@NonNull final Ray ray, @NonNull final SceneInstance scene) {
        var nearest = RayUtil.MISS;
        for (final var terrain : scene.getTerrainSurfaces())
            if (terrain instanceof TerrainSurfaceInstanceImplementation entity) {
                final var distance = terrain(ray, entity, MAXIMAL_DISTANCE);
                if (distance != RayUtil.MISS && (nearest == RayUtil.MISS || distance < nearest))
                    nearest = distance;
            }

        return nearest;
    }

    /**
     * Where a ray meets the surface of the water (without the waves), {@link RayUtil#MISS} when it doesn't or it is beside the water.
     */
    public float water(@NonNull final Ray ray, @NonNull final WaterSurfaceInstanceImplementation water) {
        final var matrix = water.getTransform().getMatrix();
        final var normal = matrix.transformDirection(new Vector3f(0.f, 1.f, 0.f)).normalize();

        final var distance = RayUtil.intersectPlane(ray, matrix.getTranslation(new Vector3f()), normal);
        if (distance == RayUtil.MISS)
            return RayUtil.MISS;

        final var local = matrix.invertAffine(new Matrix4f()).transformPosition(new Vector3f(ray.origin()).fma(distance, ray.direction()));
        return Math.abs(local.x) <= water.getGridMesh().getSize().x() * 0.5f && Math.abs(local.z) <= water.getGridMesh().getSize().y() * 0.5f ? distance : RayUtil.MISS;
    }

    /**
     * The box of a terrain in its own axes: the size of its grid, and as high as its heights go.
     */
    public BoundingBox box(@NonNull final TerrainSurfaceInstanceImplementation terrain) {
        final var size = terrain.getGridMesh().getSize();
        final var half = Math.max(Math.abs(terrain.getFactor()) * 0.5f, WATER_THICKNESS);

        return new BoundingBox(new Vector3f(-size.x() * 0.5f, -half, -size.y() * 0.5f), new Vector3f(size.x() * 0.5f, half, size.y() * 0.5f));
    }

    /**
     * The box of the water in its own axes: the size of its grid, and a thin layer round its surface.
     */
    public BoundingBox box(@NonNull final WaterSurfaceInstanceImplementation water) {
        final var size = water.getGridMesh().getSize();
        final var half = Math.max(water.getWaveAmplitude(), WATER_THICKNESS * 0.5f);

        return new BoundingBox(new Vector3f(-size.x() * 0.5f, -half, -size.y() * 0.5f), new Vector3f(size.x() * 0.5f, half, size.y() * 0.5f));
    }

    private float narrow(final Ray ray, final BiFunction<Float, Float, Float> height, final float above, final float under) {
        var low = above;
        var high = under;

        for (int step = 0; step < BISECTIONS; step++) {
            final var middle = (low + high) * 0.5f;
            final var point = new Vector3f(ray.origin()).fma(middle, ray.direction());

            if (point.y > height.apply(point.x, point.z))
                low = middle;
            else
                high = middle;
        }

        return (low + high) * 0.5f;
    }
}
