package io.github.trimax.venta.engine.model.common.scene;

import io.github.trimax.venta.engine.enums.FogType;
import org.joml.Vector3f;

public record Fog(
        Vector3f color,
        float density
) {
    public static Fog exponential(final Vector3f color, final float density) {
        return new Fog(color, density);
    }
}