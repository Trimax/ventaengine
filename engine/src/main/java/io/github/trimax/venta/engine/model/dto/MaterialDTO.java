package io.github.trimax.venta.engine.model.dto;

import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.enums.TextureType;

public record MaterialDTO(Float metalness,
                          Float roughness,
                          Float opacity,
                          Vector3f color,
                          Vector2f tiling,
                          Vector2f offset,
                          Map<TextureType, String> textures) {
    public boolean hasTextures() {
        return textures != null && !textures.isEmpty();
    }
}

