package io.github.trimax.venta.engine.registries;

import io.github.trimax.venta.engine.enums.TextureColorSpace;

import io.github.trimax.venta.engine.model.entity.TextureEntity;

/**
 * Textures by their resource path: without an argument (or with {@link TextureColorSpace#Color}) a texture is a color
 * stored in sRGB, with {@link TextureColorSpace#Data} its values are linear.
 */
public interface TextureRegistry extends AbstractRegistry<TextureEntity, TextureColorSpace> {
}
