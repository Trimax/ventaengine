package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.CubemapFace;
import lombok.NonNull;

import java.util.Map;

public record CubemapDTO(String program, Map<CubemapFace, String> faces) {
    public String getTexturePath(@NonNull final CubemapFace face) {
        return faces.getOrDefault(face, null);
    }
}
