package io.github.trimax.venta.engine.utils;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Colors of the definitions (materials, lights, fog, water) are written in sRGB like in any color picker, while lighting
 * is computed with linear values; the final image is encoded back to sRGB by the tone mapping pass.
 */
@UtilityClass
public final class ColorUtil {
    /* The sRGB transfer function (IEC 61966-2-1) */
    private static final float LINEAR_LIMIT = 0.0031308f;
    private static final float SRGB_LIMIT = 0.04045f;
    private static final float LINEAR_SLOPE = 12.92f;
    private static final float OFFSET = 0.055f;
    private static final float GAMMA = 2.4f;

    /** An sRGB channel to a linear one; values above 1 (bright HDR colors) follow the same curve */
    public float toLinear(final float value) {
        if (value <= SRGB_LIMIT)
            return value / LINEAR_SLOPE;

        return (float) Math.pow((value + OFFSET) / (1.f + OFFSET), GAMMA);
    }

    /** An sRGB color to a linear one */
    public Vector3f toLinear(@NonNull final Vector3fc color, @NonNull final Vector3f destination) {
        return destination.set(toLinear(color.x()), toLinear(color.y()), toLinear(color.z()));
    }

    /** An sRGB color to a linear one, the alpha is not a color and is kept */
    public Vector4f toLinear(@NonNull final Vector4fc color, @NonNull final Vector4f destination) {
        return destination.set(toLinear(color.x()), toLinear(color.y()), toLinear(color.z()), color.w());
    }

    /** A linear channel to an sRGB one */
    public float toSRGB(final float value) {
        if (value <= LINEAR_LIMIT)
            return value * LINEAR_SLOPE;

        return (1.f + OFFSET) * (float) Math.pow(value, 1.f / GAMMA) - OFFSET;
    }
}
