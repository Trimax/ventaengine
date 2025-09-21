package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.lwjgl.openal.AL10.*;

@Getter
public final class CameraInstanceImplementation extends AbstractInstanceImplementation implements CameraInstance {
    private static final Vector3f worldUp = new Vector3f(0, 1, 0);

    private final Vector3f position = new Vector3f(0.f);
    private final Vector3f rotation = new Vector3f(0.f); // pitch (X), yaw (Y), roll (Z)

    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);

    CameraInstanceImplementation(@NonNull final String name,
                                 @NonNull final Vector3fc position,
                                 @NonNull final Vector3fc target,
                                 @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        setPosition(position);
        lookAt(target);
    }

    private void rotateAround(@NonNull final Vector3fc axis, final float angleRadians) {
        final var rotationMatrix = new Matrix3f().rotate(angleRadians, axis);

        front.mul(rotationMatrix).normalize();
        up.mul(rotationMatrix).normalize();
        right.mul(rotationMatrix).normalize();

        updateListener();
    }

    private void updateListener() {
        alListener3f(AL_POSITION, position.x(), position.y(), position.z());
        alListenerfv(AL_ORIENTATION, new float[] {
                front.x(), front.y(), front.z(),
                up.x(), up.y(), up.z()
        });
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    @Override
    public void move(@NonNull final Vector3fc direction) {
        position.add(direction);
        updateListener();
    }

    @Override
    public void moveForward(final float distance) {
        position.fma(distance, front);
        updateListener();
    }

    @Override
    public void moveRight(final float distance) {
        position.fma(distance, right);
        updateListener();
    }

    @Override
    public void moveUp(final float distance) {
        position.fma(distance, worldUp);
        updateListener();
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
    public void setPosition(@NonNull final Vector3fc newPosition) {
        this.position.set(newPosition);
        updateListener();
    }

    @Override
    public void lookAt(@NonNull final Vector3fc target) {
        front.set(new Vector3f(target).sub(position).normalize());

        final var tempUp = new Vector3f(0, 1, 0);
        if (Math.abs(front.dot(tempUp)) > 0.999f)
            tempUp.set(0, 0, 1);

        right.set(new Vector3f(front).cross(tempUp).normalize());
        up.set(new Vector3f(right).cross(front).normalize());

        updateListener();
    }
}
