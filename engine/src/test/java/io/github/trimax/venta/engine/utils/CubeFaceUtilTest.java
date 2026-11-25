package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

/**
 * The cameras of the faces of a cube map draw what a shader reads from the face with a direction. The cube map of OpenGL says
 * where a direction is on a face (the table of the specification: the major axis, and the coordinates s and t on the face), and a
 * point of that direction must be there on the picture of the camera of the face.
 */
final class CubeFaceUtilTest {
    /** Where a direction is on a face by the table of the specification of OpenGL: s and t from -1 to 1 */
    private static float[] expected(final int face, final Vector3f direction) {
        final var x = direction.x;
        final var y = direction.y;
        final var z = direction.z;

        return switch (face) {
            case 0 -> new float[]{-z / x, -y / x};
            case 1 -> new float[]{z / -x, -y / -x};
            case 2 -> new float[]{x / y, z / y};
            case 3 -> new float[]{x / -y, -z / -y};
            case 4 -> new float[]{x / z, -y / z};
            default -> new float[]{-x / -z, -y / -z};
        };
    }

    /** Directions that are inside the field of view of every face: the face axis is the strongest */
    private static Vector3f direction(final int face) {
        final var axis = face / 2;
        final var sign = face % 2 == 0 ? 1.f : -1.f;

        final var result = new Vector3f(0.3f, -0.4f, 0.2f);
        result.setComponent(axis, sign);
        return result;
    }

    @Test
    void testPointsAreWhereTheCubeMapSaysTheyAre() {
        for (int face = 0; face < CubeFaceUtil.FACES; face++) {
            final var view = CubeFaceUtil.getView(face, new Vector3f(), new Matrix4f());
            final var projection = CubeFaceUtil.getProjection(0.1f, 100.f, new Matrix4f());

            final var direction = direction(face);
            final var clip = new Vector4f();
            new Matrix4f(projection).mul(view).transform(new Vector4f(direction, 1.f), clip);

            final var expected = expected(face, direction);
            assertEquals(expected[0], clip.x / clip.w, 1e-4f, "s of the face " + face);
            assertEquals(expected[1], clip.y / clip.w, 1e-4f, "t of the face " + face);
            assertTrue(clip.w > 0.f, "The face " + face + " looks at it");
        }
    }

    @Test
    void testViewMovesWithThePosition() {
        final var position = new Vector3f(3.f, 5.f, -7.f);
        for (int face = 0; face < CubeFaceUtil.FACES; face++) {
            final var view = CubeFaceUtil.getView(face, position, new Matrix4f());

            final var origin = view.transformPosition(new Vector3f(position));
            assertEquals(0.f, origin.length(), 1e-5f, "The camera of the face " + face + " is at the position");
        }
    }

    @Test
    void testCubeHasSixFaces() {
        assertThrows(IllegalArgumentException.class, () -> CubeFaceUtil.getView(-1, new Vector3f(), new Matrix4f()));
        assertThrows(IllegalArgumentException.class, () -> CubeFaceUtil.getView(6, new Vector3f(), new Matrix4f()));
    }

    @Test
    void testProjectionIsSquareAndWide() {
        final var projection = CubeFaceUtil.getProjection(0.1f, 100.f, new Matrix4f());

        assertEquals(1.f, projection.m00(), 1e-5f, "90 degrees: the edge of the picture is at 45 degrees");
        assertEquals(1.f, projection.m11(), 1e-5f);
    }
}
