package io.github.trimax.venta.engine.model.common.dto;

import org.joml.Vector4f;

public record Color(float r, float g, float b, float a) {
    public Vector4f toVector4f() {
        return new Vector4f(r, g, b, a);
    }
}
