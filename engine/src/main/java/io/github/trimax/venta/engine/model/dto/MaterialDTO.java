package io.github.trimax.venta.engine.model.dto;

import java.util.Map;

import org.joml.Vector2f;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;

public record MaterialDTO(Float metalness,
                          Float roughness,
                          Float opacity,
                          ColorDTO color,
                          Vector2f tiling,
                          Vector2f offset,
                          Map<TextureType, String> textures) {
    public boolean hasTextures() {
        return textures != null && !textures.isEmpty();
    }
}

