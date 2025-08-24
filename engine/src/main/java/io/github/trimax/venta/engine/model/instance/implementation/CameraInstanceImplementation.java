package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Getter
public final class CameraInstanceImplementation extends AbstractInstanceImplementation implements CameraInstance {
    private static final Vector3f worldUp = new Vector3f(0, 1, 0);

    private final Vector3f position = new Vector3f(0.f);
    private final Vector3f rotation = new Vector3f(0.f); // pitch (X), yaw (Y), roll (Z)

    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);

    CameraInstanceImplementation(@NonNull final String name,
                                 @NonNull final Vector3f position,
                                 @NonNull final Vector3f target,
                                 @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        setPosition(position);
        lookAt(target);
    }

    private void rotateAround(final Vector3f axis, final float angleRadians) {
        final var rotationMatrix = new Matrix3f().rotate(angleRadians, axis);

        front.mul(rotationMatrix).normalize();
        up.mul(rotationMatrix).normalize();
        right.mul(rotationMatrix).normalize();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    @Override
    public void moveForward(final float distance) {
        position.fma(distance, front);
    }

    @Override
    public void moveRight(final float distance) {
        position.fma(distance, right);
    }

    @Override
    public void moveUp(final float distance) {
        position.fma(distance, worldUp);
    }

    @Override
    public void rotateYaw(final float radians) {
        rotateAround(up, radians);
    }

    @Override
    public void rotatePitch(final float radians) {
        rotateAround(right, radians);
    }

    @Override
    public void rotateRoll(final float radians) {
        rotateAround(front, radians);
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    @Override
    public void setPosition(final Vector3f newPosition) {
        this.position.set(newPosition);
    }

    @Override
    public void lookAt(final Vector3f target) {
        front.set(new Vector3f(target).sub(position).normalize());

        final var tempUp = new Vector3f(0, 1, 0);
        if (Math.abs(front.dot(tempUp)) > 0.999f)
            tempUp.set(0, 0, 1);

        right.set(new Vector3f(front).cross(tempUp).normalize());
        up.set(new Vector3f(right).cross(front).normalize());
    }
}
