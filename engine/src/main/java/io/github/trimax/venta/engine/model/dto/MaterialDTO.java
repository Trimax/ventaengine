package io.github.trimax.venta.engine.model.dto;

import java.util.Map;

import org.joml.Vector2f;

import io.github.trimax.venta.engine.enums.TextureType;

public record MaterialDTO(Float shininess,
                          Float opacity,
                          Vector2f tiling,
                          Vector2f offset,
                          Map<TextureType, String> textures) {
}

