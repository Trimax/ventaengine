package io.github.trimax.venta.engine.enums;

/**
 * The kind of a light.
 */
public enum LightType {
    /** Shines to all the directions equally */
    Point,

    /** Shines in a cone along its direction: full light inside the inner angle, fading to nothing at the outer one */
    Spot
}
