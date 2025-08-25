package io.github.trimax.venta.engine.model.common.scene;

import io.github.trimax.venta.engine.enums.FogType;
import org.joml.Vector4f;

public record Fog(
        FogType type,
        Vector4f color,
        float density,
        float start,
        float end
) {
    public static Fog linear(final Vector4f color, final float start, final float end) {
        return new Fog(FogType.Linear, color, 0.1f, start, end);
    }

    public static Fog exponential(final Vector4f color, final float density) {
        return new Fog(FogType.Exp, color, density, 0.0f, 0.0f);
    }
}