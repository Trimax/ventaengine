package io.github.trimax.venta.engine.enums;

import com.google.gson.annotations.SerializedName;

/**
 * What the alpha channel of a material (its color multiplied by its diffuse texture) does.
 */
public enum AlphaMode {
    /** The alpha mixes the surface with what is behind it: glass, smoke. Such objects are drawn from far to near. */
    @SerializedName(value = "blend", alternate = {"Blend", "BLEND"})
    Blend,

    /**
     * The alpha is a hole or a surface: fragments with the alpha below the cutoff are discarded, the others are
     * opaque and write depth. Leaves, grass, fences: they need no sorting and cast the shadows of their shape.
     */
    @SerializedName(value = "cutout", alternate = {"Cutout", "CUTOUT"})
    Cutout,

    /**
     * The alpha is ignored: the surface is opaque, whatever its color and its texture say. Buildings, rocks, trunks: they
     * need no sorting, and the objects that have only such surfaces are drawn with instancing like cutout ones.
     */
    @SerializedName(value = "opaque", alternate = {"Opaque", "OPAQUE"})
    Opaque
}
