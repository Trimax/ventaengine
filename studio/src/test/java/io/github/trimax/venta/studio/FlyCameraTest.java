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

    @Test
    void testOrbitKeepsTheDistanceToThePivotAndLooksAtIt() {
        final var camera = framed();
        final var distance = camera.getPosition().distance(camera.getPivot());

        camera.orbit(120, -40);

        assertEquals(distance, camera.getPosition().distance(camera.getPivot()), 1e-3f);
        final var toPivot = new Vector3f(camera.getPivot()).sub(camera.getPosition()).normalize();
        assertEquals(Math.asin(toPivot.y), camera.getPitch(), 1e-3f, "It looks at the pivot");
        assertEquals(Math.atan2(toPivot.x, -toPivot.z), camera.getYaw(), 1e-3f);
    }

    @Test
    void testOrbitDraggedToTheRightTurnsTheSceneToTheRight() {
        final var camera = framed();
        camera.view(CameraView.Front);
        final var yaw = camera.getYaw();

        camera.orbit(100, 0);

        assertTrue(camera.getYaw() > yaw, "The camera goes to the left round the scene, so the view turns to the right");
        assertTrue(camera.getPosition().x < camera.getPivot().x);
    }

    @Test
    void testOrbitNeverGoesOverThePoles() {
        final var camera = framed();

        camera.orbit(0, 1_000_000);
        assertTrue(camera.getPosition().y > camera.getPivot().y);
        assertFalse(Float.isNaN(camera.getPosition().x));

        camera.orbit(0, -2_000_000);
        assertTrue(camera.getPosition().y < camera.getPivot().y);
    }

    @Test
    void testPanMovesTheCameraAndThePivotTogether() {
        final var camera = framed();
        camera.view(CameraView.Front);
        final var distance = camera.getPosition().distance(camera.getPivot());
        final var x = camera.getPosition().x;
        final var pivotX = camera.getPivot().x;

        camera.pan(100, 0);

        assertTrue(camera.getPosition().x < x, "The scene follows the mouse to the right: the camera goes to the left");
        assertEquals(camera.getPosition().x - x, camera.getPivot().x - pivotX, 1e-4f);
        assertEquals(distance, camera.getPosition().distance(camera.getPivot()), 1e-3f);
    }

    @Test
    void testFocusPutsTheThingInFrontOfTheCameraWithoutTurningIt() {
        final var camera = framed();
        camera.look(30, 20);
        final var yaw = camera.getYaw();
        final var pitch = camera.getPitch();

        camera.focus(new Vector3f(3.f, 1.f, 2.f), 2.f);

        assertEquals(yaw, camera.getYaw(), EPSILON);
        assertEquals(pitch, camera.getPitch(), EPSILON);
        assertEquals(new Vector3f(3.f, 1.f, 2.f), camera.getPivot());
        assertEquals(5.f, camera.getPosition().distance(camera.getPivot()), 1e-3f, "A thing of the radius 2 is looked at from 2.5 radiuses");
    }

    @Test
    void testFocusOnASmallThingIsNotTooClose() {
        final var camera = framed();

        camera.focus(new Vector3f(), 0.f);

        assertTrue(camera.getPosition().distance(camera.getPivot()) > 0.5f);
    }
}
