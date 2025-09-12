package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.dto.Color;
import org.joml.Vector2f;

import java.util.Map;

public record MaterialDTO(Float metalness,
                          Float roughness,
                          Float opacity,
                          Color color,
                          Vector2f tiling,
                          Vector2f offset,
                          Map<TextureType, String> textures) {
    public boolean hasTextures() {
        return textures != null && !textures.isEmpty();
    }
}

