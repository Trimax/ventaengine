package io.github.trimax.venta.engine.definitions;

import lombok.experimental.UtilityClass;
import org.joml.*;

@UtilityClass
public final class DefinitionsCommon {
    /* Limitations */
    public static final int MAX_MATERIALS = 8;
    public static final int MAX_LIGHTS = 64;
    public static final int MAX_NOISES = 16;
    public static final int MAX_WAVES = 16;

    /* Vectors 4D */
    public static final Vector4fc VECTOR4F_ZERO = new Vector4f(0.f);
    public static final Vector4fc VECTOR4F_ONE = new Vector4f(1.f);

    /* Vectors 3D */
    public static final Vector3fc VECTOR3F_ZERO = new Vector3f(0.f);
    public static final Vector3fc VECTOR3F_ONE = new Vector3f(1.f);

    /* Vectors 2D */
    public static final Vector2fc VECTOR2F_ZERO = new Vector2f(0.f);
    public static final Vector2fc VECTOR2F_ONE = new Vector2f(1.f);
}
