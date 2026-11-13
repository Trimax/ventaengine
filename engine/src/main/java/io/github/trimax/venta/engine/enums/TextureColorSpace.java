package io.github.trimax.venta.engine.enums;

/**
 * What the values of a texture are.
 */
public enum TextureColorSpace {
    /** Colors (diffuse maps, skyboxes, sprites) are stored in sRGB and read as linear values by the GPU */
    Color,

    /** Data (normal, roughness, metalness and height maps) is linear and read as it is */
    Data
}
