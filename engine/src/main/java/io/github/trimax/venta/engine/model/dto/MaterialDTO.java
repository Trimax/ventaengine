package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.TextureType;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Map;

public record MaterialDTO(Float shininess,
                          Float opacity,
                          Vector3f color,
                          Vector2f tiling,
                          Vector2f offset,
                          Map<TextureType, String> textures) {
}

