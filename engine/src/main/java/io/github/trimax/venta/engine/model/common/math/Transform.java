package io.github.trimax.venta.engine.model.common.math;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@ToString
@NoArgsConstructor
public final class Transform {
    private final Matrix4f matrix = new Matrix4f();

    @Getter
    private final Vector3f position = new Vector3f(0, 0, 0);

    @Getter
    private final Vector3f rotation = new Vector3f(0, 0, 0);

    @Getter
    private final Vector3f scale = new Vector3f(1, 1, 1);

    private boolean dirty = true;

    public Transform(@NonNull final Transform transform) {
        setPosition(transform.getPosition());
        setRotation(transform.getRotation());
        setScale(transform.getScale());
    }

    public void setPosition(@NonNull final Vector3fc position) {
        this.position.set(position);
        this.dirty = true;
    }

    public void setRotation(@NonNull final Vector3fc rotation) {
        this.rotation.set(rotation);
        this.dirty = true;
    }

    public void setScale(@NonNull final Vector3fc scale) {
        this.scale.set(scale);
        this.dirty = true;
    }

    public void move(@NonNull final Vector3fc offset) {
        this.position.add(offset, this.position);
        this.dirty = true;
    }

    public void rotate(@NonNull final Vector3fc angles) {
        this.rotation.add(angles, this.rotation);
        this.dirty = true;
    }

    public void scale(@NonNull final Vector3fc factor) {
        this.scale.add(factor, this.scale);
        this.dirty = true;
    }

    public Matrix4f getMatrix() {
        if (dirty)
            matrix.identity()
                    .translate(position)
                    .rotateXYZ(rotation.x, rotation.y, rotation.z)
                    .scale(scale);
        dirty = false;

        return matrix;
    }
}
