package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shadow.PointShadowFrame;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;

final class PointShadowHelperTest {
    private static final float EPSILON = 1e-4f;

    private final PointShadowHelper helper = new PointShadowHelper();
    private final PointShadowFrame frame = new PointShadowFrame(DefinitionsCommon.MAX_POINT_SHADOWS, DefinitionsCommon.POINT_SHADOW_FACES);

    private static LightInstanceImplementation light(final String name, final float x, final float range, final boolean castShadows) {
        return TestObjects.light(name, new Vector3f(x, 0, 0), range, castShadows);
    }

    private static List<LightInstanceImplementation> lights(final LightInstanceImplementation... lights) {
        return List.of(lights);
    }

    @Test
    void testOnlyLightsThatCastShadowsAndAreOnGetAMap() {
        final var casting = light("casting", 1.f, 20.f, true);
        final var plain = light("plain", 2.f, 20.f, false);
        final var off = light("off", 3.f, 20.f, true);
        off.setEnabled(false);
        final var dark = light("dark", 4.f, 20.f, true);
        dark.setIntensity(0.f);

        helper.assign(frame, lights(casting, plain, off, dark), new Vector3f());

        assertEquals(1, frame.getCount());
        assertEquals(0, frame.getSlot(casting));
        assertEquals(-1, frame.getSlot(plain));
        assertEquals(-1, frame.getSlot(off));
        assertEquals(-1, frame.getSlot(dark), "A light that gives no light casts no shadows");
    }

    @Test
    void testTheNearestLightsToTheCameraGetTheMaps() {
        final var all = new ArrayList<LightInstanceImplementation>();
        for (int i = 0; i < 7; i++)
            all.add(light("light " + i, 10.f * (7 - i), 20.f, true));

        helper.assign(frame, all, new Vector3f());

        assertEquals(DefinitionsCommon.MAX_POINT_SHADOWS, frame.getCount());
        // The lights 6, 5, 4 and 3 are the nearest: at 10, 20, 30 and 40
        for (int i = 3; i < 7; i++)
            assertNotEquals(-1, frame.getSlot(all.get(i)), "The light " + i + " is near");

        for (int i = 0; i < 3; i++)
            assertEquals(-1, frame.getSlot(all.get(i)), "The light " + i + " is far");
    }

    @Test
    void testSlotsGoFromTheNearestLightToTheFarthest() {
        final var far = light("far", 30.f, 20.f, true);
        final var near = light("near", 5.f, 20.f, true);
        final var middle = light("middle", 15.f, 20.f, true);

        helper.assign(frame, lights(far, near, middle), new Vector3f());

        assertEquals(0, frame.getSlot(near));
        assertEquals(1, frame.getSlot(middle));
        assertEquals(2, frame.getSlot(far));
    }

    @Test
    void testDistanceIsMeasuredFromTheCamera() {
        final var left = light("left", -50.f, 20.f, true);
        final var right = light("right", 50.f, 20.f, true);

        helper.assign(frame, lights(left, right), new Vector3f(45.f, 0.f, 0.f));

        assertEquals(0, frame.getSlot(right), "The camera is next to it");
        assertEquals(1, frame.getSlot(left));
    }

    @Test
    void testFrameIsFilledAgainEveryFrame() {
        final var first = light("first", 1.f, 20.f, true);
        final var second = light("second", 2.f, 20.f, true);

        helper.assign(frame, lights(first, second), new Vector3f());
        helper.assign(frame, lights(second), new Vector3f());

        assertEquals(1, frame.getCount());
        assertEquals(0, frame.getSlot(second));
        assertEquals(-1, frame.getSlot(first), "A light that stopped casting shadows has no map");

        helper.assign(frame, lights(), new Vector3f());
        assertFalse(frame.isActive());
    }

    @Test
    void testLightWithoutARangeGetsTheDefaultOne() {
        final var light = light("light", 1.f, 0.f, true);

        helper.assign(frame, lights(light), new Vector3f());

        assertEquals(DefinitionsCommon.DEFAULT_POINT_SHADOW_RANGE, frame.getRange(0));
        assertEquals(DefinitionsCommon.DEFAULT_POINT_SHADOW_RANGE, PointShadowHelper.getRange(light));
    }

    @Test
    void testRangeOfALightIsTheFarPlaneOfItsFaces() {
        final var light = light("light", 0.f, 30.f, true);
        helper.assign(frame, lights(light), new Vector3f());

        // The point on an axis at the range is on the far plane: its depth is 1 in the clip space
        for (int face = 0; face < 6; face++) {
            final var direction = PointShadowHelper.FACE_DIRECTIONS[face];
            final var clip = new Vector4f(direction.x() * 30.f, direction.y() * 30.f, direction.z() * 30.f, 1.f).mul(frame.getMatrix(0, face));
            final var depth = clip.z / clip.w;

            assertEquals(1.f, depth, 1e-3f, "The far plane of the face " + face);
        }
    }

    @Test
    void testEveryFaceSeesTheDirectionItLooksAlong() {
        final var light = TestObjects.light("light", new Vector3f(3.f, -2.f, 5.f), 40.f, true);
        helper.assign(frame, lights(light), new Vector3f());

        for (int face = 0; face < 6; face++) {
            final var direction = PointShadowHelper.FACE_DIRECTIONS[face];
            final var point = new Vector4f(3.f + direction.x() * 10.f, -2.f + direction.y() * 10.f, 5.f + direction.z() * 10.f, 1.f);
            final var clip = new Vector4f(point).mul(frame.getMatrix(0, face));

            assertEquals(0.f, clip.x / clip.w, EPSILON, "The center of the face " + face + " in x");
            assertEquals(0.f, clip.y / clip.w, EPSILON, "The center of the face " + face + " in y");
            assertTrue(clip.z / clip.w > -1.f && clip.z / clip.w < 1.f, "In front of the face " + face + ": " + clip.z / clip.w);
        }
    }

    @Test
    void testFacesCoverAllTheDirectionsAndOverlapAtTheEdges() {
        final var light = light("light", 0.f, 40.f, true);
        helper.assign(frame, lights(light), new Vector3f());

        // Every direction is seen by the face it lies the most along, with the filter of a shadow still inside the face
        final var random = new java.util.Random(7);
        for (int i = 0; i < 2000; i++) {
            final var direction = new Vector3f(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1);
            if (direction.lengthSquared() < 1e-3f)
                continue;

            final var face = faceOf(direction);
            final var clip = new Vector4f(direction.x * 10.f / direction.length(), direction.y * 10.f / direction.length(), direction.z * 10.f / direction.length(), 1.f)
                    .mul(frame.getMatrix(0, face));

            assertTrue(Math.abs(clip.x / clip.w) <= 1.f / PointShadowHelper.FACE_TANGENT + EPSILON, "In x of the face " + face + ": " + clip.x / clip.w);
            assertTrue(Math.abs(clip.y / clip.w) <= 1.f / PointShadowHelper.FACE_TANGENT + EPSILON, "In y of the face " + face + ": " + clip.y / clip.w);
        }

        assertTrue(1.f / PointShadowHelper.FACE_TANGENT < 1.f, "The faces see more than the 90 degrees that touch them");
    }

    /** The face by the axis that the direction lies the most along: the rule the shaders use */
    private static int faceOf(final Vector3f direction) {
        final var absolute = new Vector3f(direction).absolute();
        if (absolute.x >= absolute.y && absolute.x >= absolute.z)
            return direction.x >= 0.f ? 0 : 1;

        if (absolute.y >= absolute.z)
            return direction.y >= 0.f ? 2 : 3;

        return direction.z >= 0.f ? 4 : 5;
    }

    @Test
    void testArgumentsAreRequired() {
        assertThrows(NullPointerException.class, () -> helper.assign(null, lights(), new Vector3f()));
        assertThrows(NullPointerException.class, () -> helper.assign(frame, null, new Vector3f()));
        assertThrows(NullPointerException.class, () -> helper.assign(frame, lights(), null));
    }
}
