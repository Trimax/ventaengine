package io.github.trimax.venta.engine.definitions;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DefinitionsCommon {
    /* Limitations */
    public static final int MAX_LIGHTS = 64;
    public static final int MAX_WAVES = 16;

    /* Default values */
    public static Vector3fc DEFAULT_SCALE_3D = new Vector3f(1.f);
    public static Vector2fc DEFAULT_SCALE_2D = new Vector2f(1.f);
}
