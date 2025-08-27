package io.github.trimax.venta.engine.model.instance;

import lombok.NonNull;
import org.joml.Vector3fc;

public interface CameraInstance extends AbstractInstance {
    Vector3fc getPosition();

    Vector3fc getRotation();

    Vector3fc getFront();

    Vector3fc getUp();

    Vector3fc getRight();

    void setPosition(@NonNull final Vector3fc position);

    void lookAt(@NonNull final Vector3fc position);

    void move(@NonNull final Vector3fc direction);

    void moveForward(final float distance);

    void moveRight(final float distance);

    void moveUp(final float distance);

    void rotateYaw(final float radians);

    void rotatePitch(final float radians);

    void rotateRoll(final float radians);
}
