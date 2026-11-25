package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Oblique near plane (E. Lengyel, "Oblique View Frustum Depth Projection and Clipping"): the near plane of a perspective
 * projection is replaced with any plane, so the hardware clips what is on one side of it and nothing else is needed (no clip
 * distances in the shaders). The reflection of water uses it to cut away what is under the water.
 */
@UtilityClass
public final class ObliqueProjectionUtil {
    /**
     * Puts a plane in place of the near plane of a projection.
     *
     * @param projection a perspective projection, it is changed
     * @param plane      the plane in the space of the camera (a, b, c, d: a x + b y + c z + d = 0): what lies on its positive
     *                   side is kept, and what lies on the negative side is clipped. The camera is on the clipped side.
     * @return the projection
     */
    public Matrix4f clip(@NonNull final Matrix4f projection, @NonNull final Vector4fc plane) {
        // The corner of the frustum that is opposite to the plane, in the space of the camera
        final var corner = projection.invert(new Matrix4f()).transform(new Vector4f(Math.signum(plane.x()), Math.signum(plane.y()), 1.f, 1.f));
        final var scaled = new Vector4f(plane).mul(2.f / plane.dot(corner));

        // The third row of the projection becomes the plane less the fourth row: the depth is 0 on the plane, and the fourth row is untouched
        return projection.m02(scaled.x - projection.m03())
                .m12(scaled.y - projection.m13())
                .m22(scaled.z - projection.m23())
                .m32(scaled.w - projection.m33());
    }
}
