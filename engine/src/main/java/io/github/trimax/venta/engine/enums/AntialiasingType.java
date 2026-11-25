package io.github.trimax.venta.engine.enums;

/**
 * How the edges of the image are smoothed after the multisampling of the geometry has done what it can.
 */
public enum AntialiasingType {
    /** The edges are left as they are, the antialiasing is switched off without taking its settings away */
    None,

    /** FXAA: a pass over the tone mapped image that blurs the edges it finds by the contrast of the brightness */
    FXAA
}
