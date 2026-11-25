package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Vector3fc;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The cameras of the six faces of a cube map, in the order of OpenGL: +x, -x, +y, -y, +z, -z. An image drawn by a camera of a
 * face with a square projection of 90 degrees is what a shader reads from the face with a direction of the cube map.
 */
@UtilityClass
public final class CubeFaceUtil {
    public static final int FACES = 6;

    /** Where every face looks, and where its up goes (the up of a face is set by the layout of the cube map, so the vertical faces differ) */
    private static final float[][] DIRECTIONS = {
            {1.f, 0.f, 0.f, 0.f, -1.f, 0.f},
            {-1.f, 0.f, 0.f, 0.f, -1.f, 0.f},
            {0.f, 1.f, 0.f, 0.f, 0.f, 1.f},
            {0.f, -1.f, 0.f, 0.f, 0.f, -1.f},
            {0.f, 0.f, 1.f, 0.f, -1.f, 0.f},
            {0.f, 0.f, -1.f, 0.f, -1.f, 0.f}
    };

    /**
     * The view matrix of a face.
     *
     * @param face     from 0 (+x) to 5 (-z)
     * @param position where the cube map is seen from
     * @param result   the matrix that is set
     * @return the result
     */
    public Matrix4f getView(final int face, @NonNull final Vector3fc position, @NonNull final Matrix4f result) {
        if (face < 0 || face >= FACES)
            throw new IllegalArgumentException("A cube has six faces, got " + face);

        final var direction = DIRECTIONS[face];
        return result.identity().lookAt(position.x(), position.y(), position.z(),
                position.x() + direction[0], position.y() + direction[1], position.z() + direction[2],
                direction[3], direction[4], direction[5]);
    }

    /** The projection of a face: the field of view of 90 degrees and the picture is square */
    public Matrix4f getProjection(final float near, final float far, @NonNull final Matrix4f result) {
        return result.identity().perspective((float) Math.PI / 2.f, 1.f, near, far);
    }
}
