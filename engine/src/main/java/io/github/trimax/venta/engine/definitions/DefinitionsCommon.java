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

    /* Vectors 3D */
    public static final Vector3fc VECTOR3F_ZERO = new Vector3f(0.f);
    public static final Vector3fc VECTOR3F_ONE = new Vector3f(1.f);

    /* Vectors 2D */
    public static final Vector2fc VECTOR2F_ZERO = new Vector2f(0.f);
    public static final Vector2fc VECTOR2F_ONE = new Vector2f(1.f);
}
