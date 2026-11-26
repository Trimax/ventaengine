package io.github.trimax.venta.studio;

import java.util.EnumSet;
import java.util.Set;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import javafx.scene.input.KeyCode;
import lombok.Getter;

/**
 * The camera of the viewport that flies: the mouse looks round, the keys move it. The angles are kept here and the camera of the
 * engine is set from them, so it never rolls: the yaw is from -z to +x, the pitch is up from the horizon (both in radians).
 */
final class FlyCamera {
    private static final float START_X = 0.f;
    private static final float START_Y = 3.4f;
    private static final float START_Z = 30.f;
    private static final float START_YAW = (float) Math.toRadians(14.0);
    private static final float START_PITCH = (float) Math.toRadians(-8.5);

    private static final float LOOK_SPEED = 0.003f;
    private static final float MOVE_SPEED = 6.f;
    private static final float FAST_FACTOR = 4.f;
    private static final float MAXIMAL_PITCH = (float) Math.toRadians(89.0);

    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private final Vector3f position = new Vector3f();
    private final Vector3f front = new Vector3f();
    private final Vector3f right = new Vector3f();
    private final Vector3f target = new Vector3f();

    @Getter
    private float yaw;

    @Getter
    private float pitch;

    FlyCamera() {
        reset();
    }

    void reset() {
        position.set(START_X, START_Y, START_Z);
        yaw = START_YAW;
        pitch = START_PITCH;
        pressed.clear();
        updateDirections();
    }

    Vector3f getPosition() {
        return position;
    }

    void press(final KeyCode key) {
        pressed.add(key);
    }

    void release(final KeyCode key) {
        pressed.remove(key);
    }

    void releaseAll() {
        pressed.clear();
    }

    /** The mouse moved by the pixels with the button held: dragging to the right turns to the right, dragging down looks down */
    void look(final double deltaX, final double deltaY) {
        yaw += (float) deltaX * LOOK_SPEED;
        pitch = Math.clamp(pitch - (float) deltaY * LOOK_SPEED, -MAXIMAL_PITCH, MAXIMAL_PITCH);
        updateDirections();
    }

    /** The wheel flies along the view */
    void dolly(final double meters) {
        position.fma((float) meters, front);
    }

    /** Moves by the keys held for the seconds that passed */
    void update(final float seconds) {
        final var speed = MOVE_SPEED * seconds * (pressed.contains(KeyCode.SHIFT) ? FAST_FACTOR : 1.f);

        if (pressed.contains(KeyCode.W))
            position.fma(speed, front);
        if (pressed.contains(KeyCode.S))
            position.fma(-speed, front);
        if (pressed.contains(KeyCode.D))
            position.fma(speed, right);
        if (pressed.contains(KeyCode.A))
            position.fma(-speed, right);
        if (pressed.contains(KeyCode.E))
            position.y += speed;
        if (pressed.contains(KeyCode.Q))
            position.y -= speed;
    }

    /** Puts the camera of the engine where this one is */
    void apply(final CameraInstance camera) {
        camera.setPosition(position);
        camera.lookAt(target.set(position).add(front));
    }

    private void updateDirections() {
        front.set((float) (Math.sin(yaw) * Math.cos(pitch)), (float) Math.sin(pitch), (float) (-Math.cos(yaw) * Math.cos(pitch)));
        right.set((float) Math.cos(yaw), 0.f, (float) Math.sin(yaw));
    }
}
