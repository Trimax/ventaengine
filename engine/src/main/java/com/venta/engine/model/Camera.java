package com.venta.engine.model;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {
    private static final Vector3f worldUp = new Vector3f(0, 1, 0);
    private final Vector3f position;
    private final Vector3f front;
    private final Vector3f right;
    private final Vector3f up;

    @Getter
    private float yaw;   // around Y

    @Getter
    private float pitch; // around X

    @Getter
    private float roll;  // around Z

    public Camera(final Vector3f position, final Vector3f target) {
        this.position = new Vector3f(position);
        this.front = new Vector3f(target).sub(position).normalize();
        this.right = new Vector3f();
        this.up = new Vector3f();

        // Compute angles
        this.yaw = (float) Math.atan2(front.z, front.x);
        this.pitch = (float) Math.asin(front.y);
        this.roll = 0;

        updateVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public void moveForward(final float distance) {
        position.fma(distance, front);
    }

    public void moveRight(final float distance) {
        position.fma(distance, right);
    }

    public void moveUp(final float distance) {
        position.fma(distance, worldUp);
    }

    public void rotateYaw(final float radians) {
        this.yaw += radians;

        updateVectors();
    }

    public void rotatePitch(final float radians) {
        this.pitch = Math.clamp(this.pitch + radians, (float) Math.toRadians(-89.0f), (float) Math.toRadians(89.0f));

        updateVectors();
    }

    public void rotateRoll(final float radians) {
        this.roll += radians;
    }

    private void updateVectors() {
        front.x = (float) (Math.cos(pitch) * Math.cos(yaw));
        front.y = (float) Math.sin(pitch);
        front.z = (float) (Math.cos(pitch) * Math.sin(yaw));
        front.normalize();

        right.set(front).cross(worldUp).normalize();
        up.set(right).cross(front).normalize();
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getFront() {
        return new Vector3f(front);
    }

    public void setPosition(final Vector3f newPosition) {
        this.position.set(newPosition);
    }

    public void setYawPitch(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;

        updateVectors();
    }

    public void lookAt(final Vector3f target) {
        final var direction = new Vector3f(target).sub(position).normalize();

        this.yaw = (float) Math.atan2(direction.z, direction.x);
        this.pitch = (float) Math.asin(direction.y);

        updateVectors();
    }
}
