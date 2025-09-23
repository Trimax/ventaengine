package io.github.trimax.venta.engine.model.dto.common;

import org.joml.Vector3f;
import org.joml.Vector4f;

public record ColorDTO(float r, float g, float b, float a) {
    public Vector3f toVector3f() {
        return new Vector3f(r, g, b);
    }

    public Vector4f toVector4f() {
        return new Vector4f(r, g, b, a);
    }
}
