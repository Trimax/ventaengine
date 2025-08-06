package io.github.trimax.venta.engine.model.common.math;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Getter
@NoArgsConstructor
public final class Transform {
    private final Matrix4f matrix = new Matrix4f();
    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f rotation = new Vector3f(0, 0, 0);
    private final Vector3f scale = new Vector3f(1, 1, 1);
    private boolean dirty = true;

    public Transform(final Transform transform) {
        setPosition(transform.getPosition());
        setRotation(transform.getRotation());
        setScale(transform.getScale());
    }

    public void setPosition(final Vector3f position) {
        this.position.set(position);
        this.dirty = true;
    }

    public void setRotation(final Vector3f rotation) {
        this.rotation.set(rotation);
        this.dirty = true;
    }

    public void setScale(final Vector3f scale) {
        this.scale.set(scale);
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
