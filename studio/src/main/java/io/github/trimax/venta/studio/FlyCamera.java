package io.github.trimax.venta.studio;

import java.util.EnumSet;
import java.util.Set;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.utils.FramingUtil;
import javafx.scene.input.KeyCode;
import lombok.Getter;

/**
 * The camera of the viewport that flies: the mouse looks round, the keys move it. The angles are kept here and the camera of the
 * engine is set from them, so it never rolls: the yaw is from -z to +x, the pitch is up from the horizon (both in radians).
 */
public final class FlyCamera {
    private static final float START_X = 0.f;
    private static final float START_Y = 3.4f;
    private static final float START_Z = 30.f;
    private static final float START_YAW = (float) Math.toRadians(14.0);
    private static final float START_PITCH = (float) Math.toRadians(-8.5);

    private static final float LOOK_SPEED = 0.003f;
    private static final float MOVE_SPEED = 6.f;
    private static final float FAST_FACTOR = 4.f;
    private static final float START_PIVOT_DISTANCE = 20.f;
    private static final float MINIMAL_ORBIT_RADIUS = 1.f;
    private static final float PAN_SPEED = 0.0015f;
    private static final float FOCUS_DISTANCE = 2.5f;
    private static final float MAXIMAL_PITCH = (float) Math.toRadians(89.0);

    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private final Vector3f position = new Vector3f();
    private final Vector3f front = new Vector3f();
    private final Vector3f right = new Vector3f();
    private final Vector3f target = new Vector3f();
    private final Vector3f pivot = new Vector3f();

    /** What the camera looks at in the views: the scene that is open, null before there is one */
    private FramingUtil.Frame frame;

    @Getter
    private float yaw;

    @Getter
    private float pitch;

    public FlyCamera() {
        reset();
    }

    /** Back to where the camera starts: round the scene that is open, or at the first place without one */
    public void reset() {
        pressed.clear();

        if (frame == null) {
            position.set(START_X, START_Y, START_Z);
            yaw = START_YAW;
            pitch = START_PITCH;
            updateDirections();
            pivot.set(position).fma(START_PIVOT_DISTANCE, front);
            return;
        }

        view(CameraView.Perspective);
    }

    /** A scene is open: the views look at it, and the camera is put round it */
    public void frame(final FramingUtil.Frame frame) {
        this.frame = frame;
        pivot.set(frame.center());
        reset();
    }

    /** Puts the camera where the view says, at a distance that holds the scene; without a scene nothing changes */
    public void view(final CameraView view) {
        if (frame == null)
            return;

        final var distance = frame.radius() * 2.2f;
        final var center = frame.center();

        switch (view) {
            case Perspective -> position.set(center).add(0.f, frame.radius() * 0.6f, distance);
            case Top -> position.set(center).add(0.f, distance, 0.f);
            case Front -> position.set(center).add(0.f, 0.f, distance);
            case Side -> position.set(center).add(distance, 0.f, 0.f);
            default -> position.set(center).add(0.f, frame.radius() * 0.6f, distance);
        }

        pivot.set(center);
        lookAt(center);
    }

    /** The point the camera orbits and the panning keeps in the middle: the chosen thing, or the middle of the scene */
    public Vector3f getPivot() {
        return pivot;
    }

    /** Orbits round the pivot: dragging to the right turns the scene to the right, dragging down looks from above */
    public void orbit(final double deltaX, final double deltaY) {
        final var offset = new Vector3f(position).sub(pivot);
        final var radius = Math.max(offset.length(), MINIMAL_ORBIT_RADIUS);

        final var azimuth = (float) Math.atan2(offset.x, offset.z) - (float) deltaX * LOOK_SPEED;
        final var elevation = Math.clamp((float) Math.asin(Math.clamp(offset.y / radius, -1.f, 1.f)) + (float) deltaY * LOOK_SPEED, -MAXIMAL_PITCH, MAXIMAL_PITCH);

        position.set(pivot).add((float) (radius * Math.sin(azimuth) * Math.cos(elevation)), (float) (radius * Math.sin(elevation)),
                (float) (radius * Math.cos(azimuth) * Math.cos(elevation)));
        lookAt(pivot);
    }

    /** Moves the camera and the pivot with it, the scene follows the mouse: the farther the pivot, the faster */
    public void pan(final double deltaX, final double deltaY) {
        final var distance = Math.max(position.distance(pivot), MINIMAL_ORBIT_RADIUS);
        final var shift = new Vector3f(right).mul(-(float) deltaX).add(0.f, (float) deltaY, 0.f).mul(distance * PAN_SPEED);

        position.add(shift);
        pivot.add(shift);
    }

    /** Looks at a thing from a distance that holds it, from where the camera looks now */
    public void focus(final Vector3f center, final float radius) {
        pivot.set(center);
        position.set(center).fma(-Math.max(radius, FramingUtil.MINIMAL_RADIUS * 0.1f) * FOCUS_DISTANCE, front);
    }

    /** Turns the camera to the point */
    void lookAt(final Vector3f point) {
        final var direction = new Vector3f(point).sub(position).normalize();

        pitch = Math.clamp((float) Math.asin(direction.y), -MAXIMAL_PITCH, MAXIMAL_PITCH);
        yaw = (float) Math.atan2(direction.x, -direction.z);
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
