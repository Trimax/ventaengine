package io.github.trimax.venta.engine.helpers;

import static io.github.trimax.venta.engine.model.instance.implementation.TestObjects.level;
import static io.github.trimax.venta.engine.model.instance.implementation.TestObjects.object;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

/**
 * The objects are cubes of the size 1: the sphere around such a cube has the radius of about 0.87. The camera sees at 90
 * degrees (the focal length is 1), so a cube at the distance d takes about 0.87 / d of the height of the screen.
 */
final class LodHelperTest {
    private static final float FOCAL_LENGTH = 1.f;
    private static final float FRAME = 0.016f;

    private final LodHelper helper = new LodHelper();

    private static Vector3f camera(final float distance) {
        return new Vector3f(0.f, 0.f, distance);
    }

    @Test
    void testObjectIsSimplerTheFartherItIs() {
        final var near = object(null, level(0.3f), level(0.1f));
        final var middle = object(null, level(0.3f), level(0.1f));
        final var far = object(null, level(0.3f), level(0.1f));
        near.setPosition(new Vector3f(0.f, 0.f, 0.f));
        middle.setPosition(new Vector3f(0.f, 0.f, -6.f));
        far.setPosition(new Vector3f(0.f, 0.f, -20.f));

        helper.update(List.of(near, middle, far), camera(2.f), FOCAL_LENGTH, FRAME);

        assertEquals(0, near.getLod(), "At the distance 2 it takes 43% of the screen");
        assertEquals(1, middle.getLod(), "At the distance 8 it takes 11% of the screen");
        assertEquals(2, far.getLod(), "At the distance 22 it takes 4% of the screen");
    }

    @Test
    void testBiggerObjectIsAsSimpleAsASmallerOneThatIsNearer() {
        final var small = object(null, level(0.3f));
        final var big = object(null, level(0.3f));
        big.setScale(new Vector3f(4.f));
        big.setPosition(new Vector3f(0.f, 0.f, -12.f));
        small.setPosition(new Vector3f(0.f, 0.f, -3.f));

        helper.update(List.of(small, big), camera(1.f), FOCAL_LENGTH, FRAME);

        assertEquals(small.getLod(), big.getLod(), "Four times the size at four times the distance is the same size on the screen");
    }

    @Test
    void testNarrowViewKeepsObjectsDetailed() {
        final var object = object(null, level(0.3f));
        object.setPosition(new Vector3f(0.f, 0.f, -6.f));

        helper.update(List.of(object), camera(2.f), FOCAL_LENGTH, FRAME);
        assertEquals(1, object.getLod());

        final var zoomed = object(null, level(0.3f));
        zoomed.setPosition(new Vector3f(0.f, 0.f, -6.f));
        helper.update(List.of(zoomed), camera(2.f), 4.f, FRAME);
        assertEquals(0, zoomed.getLod(), "The same object looks four times bigger with a four times longer focal length");
    }

    @Test
    void testObjectsWithoutSimplerModelsAndHiddenOnesAreLeftAlone() {
        final var plain = object(null);
        final var hidden = object(null, level(0.3f));
        hidden.setVisible(false);
        hidden.setPosition(new Vector3f(0.f, 0.f, -100.f));
        plain.setPosition(new Vector3f(0.f, 0.f, -100.f));

        helper.update(List.of(plain, hidden), camera(0.f), FOCAL_LENGTH, FRAME);

        assertEquals(0, plain.getLod());
        assertEquals(0, hidden.getLod(), "A hidden object is not looked at");
    }

    @Test
    void testChangeOfTheLevelFadesOverTheTimeOfTheFrames() {
        final var object = object(null, level(0.3f));
        object.setPosition(new Vector3f(0.f, 0.f, -30.f));

        helper.update(List.of(object), camera(0.f), FOCAL_LENGTH, FRAME);
        assertEquals(1, object.getLod());
        assertEquals(0, object.getPreviousLod(), "The near model is still there");

        var frames = 1;
        while (object.getPreviousLod() >= 0 && frames < 1000) {
            helper.update(List.of(object), camera(0.f), FOCAL_LENGTH, FRAME);
            frames++;
        }

        assertEquals(-1, object.getPreviousLod());
        assertEquals(Math.round(LodHelper.FADE_DURATION / FRAME), frames, 2, "The fade takes " + LodHelper.FADE_DURATION + " seconds");
    }

    @Test
    void testObjectThatFloatsAroundABorderKeepsItsLevel() {
        final var object = object(null, level(0.3f));
        object.setPosition(new Vector3f(0.f, 0.f, 0.f));

        // The size 0.3 is at the distance of about 2.9: the camera shakes around it
        helper.update(List.of(object), camera(2.9f), FOCAL_LENGTH, FRAME);
        final var level = object.getLod();
        for (int frame = 0; frame < 100; frame++)
            helper.update(List.of(object), camera(2.9f + (frame % 2 == 0 ? 0.05f : -0.05f)), FOCAL_LENGTH, FRAME);

        assertEquals(level, object.getLod());
        assertEquals(-1, object.getPreviousLod(), "It doesn't fade back and forth");
    }

    @Test
    void testArgumentsAreRequired() {
        assertThrows(NullPointerException.class, () -> helper.update(null, camera(1.f), FOCAL_LENGTH, FRAME));
        assertThrows(NullPointerException.class, () -> helper.update(List.of(), null, FOCAL_LENGTH, FRAME));
    }
}
