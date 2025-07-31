package io.github.trimax.venta.engine.model.view;

import org.joml.Vector3f;

public interface CameraView extends AbstractView {
    Vector3f getPosition();

    void setPosition(final Vector3f position);

    void lookAt(final Vector3f position);

    void moveForward(final float distance);

    void moveRight(final float distance);

    void moveUp(final float distance);

    void rotateYaw(final float radians);

    void rotatePitch(final float radians);

    void rotateRoll(final float radians);
}
