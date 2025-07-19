package com.venta.engine.model.math;

public record Vector3(float x, float y, float z) {
    public Vector3 add(final float dx, final float dy, final float dz) {
        return new Vector3(x + dx, y + dy, z + dz);
    }

    public Vector3 add(final Vector3 delta) {
        return add(delta.x, delta.y, delta.z);
    }
}
