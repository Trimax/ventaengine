package io.github.trimax.venta.studio;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.utils.FramingUtil;
import javafx.scene.input.KeyCode;

final class FlyCameraTest {
    private static final float EPSILON = 1e-4f;

    private static FlyCamera framed() {
        final var camera = new FlyCamera();
        camera.frame(new FramingUtil.Frame(new Vector3f(10.f, 2.f, -5.f), 10.f));
        return camera;
    }

    @Test
    void testDraggingToTheRightTurnsToTheRight() {
        final var camera = new FlyCamera();
        final var yaw = camera.getYaw();

        camera.look(100, 0);

        assertTrue(camera.getYaw() > yaw);
    }

    @Test
    void testDraggingDownLooksDownAndNeverPastTheVertical() {
        final var camera = new FlyCamera();
        final var pitch = camera.getPitch();

        camera.look(0, 50);
        assertTrue(camera.getPitch() < pitch);

        camera.look(0, 1_000_000);
        assertTrue(camera.getPitch() > -Math.PI / 2, "The camera doesn't turn over");
        camera.look(0, -2_000_000);
        assertTrue(camera.getPitch() < Math.PI / 2);
    }

    @Test
    void testKeysMoveTheCameraWhileTheyAreHeld() {
        final var camera = new FlyCamera();
        final var x = camera.getPosition().x;

        camera.press(KeyCode.D);
        camera.update(1.f);
        final var moved = camera.getPosition().x;
        camera.release(KeyCode.D);
        camera.update(1.f);

        assertTrue(moved > x, "D goes to the right");
        assertEquals(moved, camera.getPosition().x, EPSILON, "A released key stops it");
    }

    @Test
    void testShiftIsFaster() {
        final var slow = new FlyCamera();
        slow.press(KeyCode.W);
        slow.update(1.f);

        final var fast = new FlyCamera();
        fast.press(KeyCode.W);
        fast.press(KeyCode.SHIFT);
        fast.update(1.f);

        assertTrue(fast.getPosition().distance(new FlyCamera().getPosition()) > slow.getPosition().distance(new FlyCamera().getPosition()) * 3.f);
    }

    @Test
    void testKeysAreForgottenWhenTheFocusIsLost() {
        final var camera = new FlyCamera();
        camera.press(KeyCode.W);
        camera.releaseAll();
        final var position = new Vector3f(camera.getPosition());
        camera.update(1.f);

        assertEquals(position, camera.getPosition());
    }

    @Test
    void testViewsLookAtTheSceneAlongTheirAxes() {
        final var camera = framed();

        camera.view(CameraView.Top);
        assertEquals(10.f, camera.getPosition().x, EPSILON);
        assertEquals(-5.f, camera.getPosition().z, EPSILON);
        assertTrue(camera.getPosition().y > 2.f);
        assertTrue(camera.getPitch() < -1.5f, "Looks down");

        camera.view(CameraView.Front);
        assertEquals(10.f, camera.getPosition().x, EPSILON);
        assertEquals(2.f, camera.getPosition().y, EPSILON);
        assertTrue(camera.getPosition().z > -5.f, "In front of the scene, looks along -z");
        assertEquals(0.f, camera.getYaw(), EPSILON);

        camera.view(CameraView.Side);
        assertTrue(camera.getPosition().x > 10.f, "At the side, looks along -x");
        assertEquals(-Math.PI / 2, camera.getYaw(), EPSILON);
    }

    @Test
    void testViewsNeedAScene() {
        final var camera = new FlyCamera();
        final var position = new Vector3f(camera.getPosition());

        camera.view(CameraView.Top);

        assertEquals(position, camera.getPosition(), "Without a scene there is nothing to look at");
    }

    @Test
    void testResetGoesBackRoundTheSceneThatIsOpen() {
        final var camera = framed();
        camera.look(300, 100);
        camera.getPosition().set(1000.f, 1000.f, 1000.f);

        camera.reset();

        assertTrue(camera.getPosition().distance(new Vector3f(10.f, 2.f, -5.f)) < 40.f);
    }

    @Test
    void testWheelFliesAlongTheView() {
        final var camera = new FlyCamera();
        final var before = new Vector3f(camera.getPosition());

        camera.dolly(5);

        assertEquals(5.f, camera.getPosition().distance(before), 1e-3f);
    }
}
