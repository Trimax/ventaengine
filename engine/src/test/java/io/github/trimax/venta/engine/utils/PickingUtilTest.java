package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;

final class PickingUtilTest {
    private final Matrix4f view = new Matrix4f().lookAt(0.f, 0.f, 10.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
    private final Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(60.0), 1.f, 0.1f, 100.f);

    private SceneInstanceImplementation scene;

    @BeforeEach
    void createScene() throws Exception {
        scene = createScene("scene");
    }

    private static SceneInstanceImplementation createScene(final String name) throws Exception {
        final var constructor = SceneInstanceImplementation.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name);
    }

    private ObjectInstanceImplementation add(final float x, final float y, final float z) {
        final var object = TestObjects.object(null);
        object.setPosition(new Vector3f(x, y, z));
        scene.add(object);
        return object;
    }

    @Test
    void testNearestObjectUnderThePointIsChosen() {
        add(0.f, 0.f, -5.f);
        final var near = add(0.f, 0.f, 0.f);

        final var result = PickingUtil.pick(scene, 0.f, 0.f, view, projection);

        assertSame(near, result.instance());
        assertTrue(result.distance() > 8.f && result.distance() < 10.f, "The camera is 10 meters from it: " + result.distance());
    }

    @Test
    void testPointWhereThereIsNothingChoosesNothing() throws Exception {
        add(0.f, 0.f, 0.f);

        assertNull(PickingUtil.pick(scene, 0.9f, 0.9f, view, projection));
        assertNull(PickingUtil.pick(createScene("empty"), 0.f, 0.f, view, projection), "An empty scene");
    }

    @Test
    void testObjectThatIsHiddenIsNotChosen() {
        final var hidden = add(0.f, 0.f, 0.f);
        final var behind = add(0.f, 0.f, -5.f);
        hidden.setVisible(false);

        assertSame(behind, PickingUtil.pick(scene, 0.f, 0.f, view, projection).instance());
    }

    @Test
    void testObjectIsChosenWhereItIsOnTheScreen() {
        final var right = add(3.f, 0.f, 0.f);

        assertSame(right, PickingUtil.pick(scene, 0.5f, 0.f, view, projection).instance(), "Three meters to the right of a camera that is 10 meters away");
        assertNull(PickingUtil.pick(scene, -0.5f, 0.f, view, projection));
    }

    @Test
    void testScaleOfAnObjectIsItsSizeOnTheScreen() {
        final var big = add(0.f, 0.f, 0.f);
        big.setScale(new Vector3f(10.f));

        assertSame(big, PickingUtil.pick(scene, 0.6f, 0.f, view, projection).instance());
    }
}
